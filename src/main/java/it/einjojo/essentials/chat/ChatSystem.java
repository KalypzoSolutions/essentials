package it.einjojo.essentials.chat;

import com.google.gson.Gson;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.papermc.paper.event.player.AsyncChatEvent;
import it.einjojo.essentials.EssentialsPlugin;
import it.einjojo.essentials.user.EssentialsUser;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * <p>Das ChatSystem sorgt für zwei Dinge:</p>
 * <ol>
 * <li>Private Nachrichten zwischen Spielern (wer, wem zuletzt geschrieben hat über Redis)</li>
 * <li>Serverübergreifende Chatnachrichten (Redis-PubSub)</li>
 * </ol>
 */
@Getter
@Slf4j
public class ChatSystem implements Listener {
    private final Gson gson = new Gson();
    private static final String LAST_PRIVATE_MSG_KEY_PREFIX = "lastPM:";
    private final PlainTextComponentSerializer plain = PlainTextComponentSerializer.plainText();
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final StatefulRedisPubSubConnection<String, String> pubSubConnection;
    private final ChatConfiguration chatConfiguration;
    private final String serverName;


    /**
     *
     * @param pubSubConnection  redis pubsub connection
     * @param plugin            plugin for event registration
     * @param chatConfiguration chat configuration
     * @param serverName        the server name where the chat system is running
     */
    public ChatSystem(@NotNull StatefulRedisPubSubConnection<String, String> pubSubConnection,
                      @NotNull JavaPlugin plugin,
                      @NotNull ChatConfiguration chatConfiguration,
                      @NotNull String serverName) {
        this.pubSubConnection = pubSubConnection;
        this.chatConfiguration = chatConfiguration;
        this.serverName = serverName;
        if (Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            throw new IllegalStateException("PlaceholderAPI is not provided! ChatSystem cannot work without it");
        }
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        pubSubConnection.addListener(new RedisPubSubAdapter<>() {
            @Override
            public void message(String channel, String message) {
                if (channel.equals("chat")) {
                    handleMessage(gson.fromJson(message, ChatMessage.class));
                }
            }
        });
        pubSubConnection.sync().subscribe("chat");
        plugin.getSLF4JLogger().info("ChatSystem initialized and listening to channel 'chat'");
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onChat(AsyncChatEvent event) {
        if (event.isCancelled()) {
            return;
        }
        try {
            String chatFormat = chatConfiguration.chatFormat();
            chatFormat = PlaceholderAPI.setPlaceholders(event.getPlayer(), chatFormat);
            Component formattedMessage = miniMessage.deserialize(chatFormat,
                    Placeholder.component("message", event.message()),
                    Placeholder.unparsed("server", serverName),
                    Placeholder.unparsed("time", LocalTime.now().format(TIME_FORMATTER))
            );
            publishNetworkChatMessage(ChatMessage.create(formattedMessage)).exceptionally((ex) -> {
                handleChatException(event, ex);
                return null;
            });
        } catch (Exception ex) {
            handleChatException(event, ex);
        }
        event.setCancelled(true);
    }

    public CompletableFuture<Long> publishNetworkChatMessage(@NotNull ChatMessage message) {

        return pubSubConnection.async().publish("chat", gson.toJson(message)).toCompletableFuture();
    }

    private void handleChatException(AsyncChatEvent event, Throwable throwable) {
        log.error("Error while sending {}'s chat message {}", event.getPlayer().getName(),
                PlainTextComponentSerializer.plainText().serialize(event.message()), throwable);
        event.getPlayer().sendMessage(Component.translatable("essentials.chat.error"));
    }


    public void handleMessage(@NotNull ChatMessage chatMessage) {
        List<Player> recipients;
        if (chatMessage.recipients() == null) {
            recipients = new ArrayList<>(Bukkit.getOnlinePlayers());
        } else {
            recipients = new LinkedList<>();
            for (UUID target : chatMessage.recipients()) {
                Player player = Bukkit.getPlayer(target);
                if (player != null) {
                    recipients.add(player);
                }
            }
        }
        for (Player recipient : recipients) {
            recipient.sendMessage(chatMessage.getContent());
        }
    }

    /**
     * Prüft, ob der Spieler auf die letzte Nachricht antworten kann.
     * Prüft aber nicht ob der Spieler, dem geantwortet werden kann, online ist.
     *
     * @param sender der Spieler, der antworten möchte
     * @return true, wenn der Spieler antworten kann
     */
    public boolean canReply(Player sender) {
        return getLastMessageSender(sender.getUniqueId()) != null;
    }

    /**
     * Gibt den letzten Spieler zurück, der dem Spieler eine Nachricht gesendet hat.
     *
     * @param uuid die UUID des Spielers
     * @return die UUID des letzten Spielers, der dem Spieler eine Nachricht gesendet hat
     */
    @Nullable
    public UUID getLastMessageSender(UUID uuid) {
        String res = pubSubConnection.sync().get(LAST_PRIVATE_MSG_KEY_PREFIX + uuid);
        if (res != null) {
            return UUID.fromString(res);
        }
        return null;
    }

    /**
     * Sendet eine private Nachricht an den letzten Spieler, der dem sender eine Nachricht gesendet hat.
     *
     * @param sender  der Spieler, der die Nachricht sendet
     * @param message die Nachricht
     */
    @Blocking
    public PrivateMessageResult replyToLastMessage(Player sender, String message) {
        UUID lastMessageSender = getLastMessageSender(sender.getUniqueId());
        if (lastMessageSender == null) {
            return PrivateMessageResult.NO_REPLY_TARGET;
        }
        Optional<EssentialsUser> target = EssentialsPlugin.environment().getUser(lastMessageSender).join();
        if (target.isEmpty()) {
            return PrivateMessageResult.RECEIVER_IS_OFFLINE;
        }
        return sendPrivateMessage(sender, target.get(), message);
    }

    /**
     * Sendet eine private Nachricht an einen Spieler.
     *
     * @param sender   der Spieler, der die Nachricht sendet
     * @param receiver der Spieler, der die Nachricht erhalten soll
     * @param message  die Nachricht
     */
    @Blocking
    public PrivateMessageResult sendPrivateMessage(Player sender, EssentialsUser receiver, String message) {
        if (receiver.hasDisabledPrivateMessages()) {
            return PrivateMessageResult.RECEIVER_DISABLED_PRIVATE_MESSAGES;
        }
        TagResolver resolver = TagResolver.builder()
                .tag("sender", Tag.selfClosingInserting(Component.text(sender.getName())))
                .tag("receiver", Tag.selfClosingInserting(Component.text(receiver.getName())))
                .tag("message", Tag.selfClosingInserting(Component.text(message)))
                .build();
        String privateMessageSender = chatConfiguration.getPrivateMessageFormatForSender();
        String privateMessageReceiver = chatConfiguration.getPrivateMessageFormatForReceiver();
        sender.sendMessage(miniMessage.deserialize(privateMessageSender, resolver));
        receiver.sendMessage(miniMessage.deserialize(privateMessageReceiver, resolver));
        pubSubConnection.sync().setex(LAST_PRIVATE_MSG_KEY_PREFIX + receiver.getUuid(), 60 * 30, sender.getUniqueId().toString());
        return PrivateMessageResult.SUCCESS;
    }

    public boolean hasDisabledPrivateMessages(UUID playerUuid) {
        var res = pubSubConnection.sync().get("disablePM:" + playerUuid);
        return Boolean.parseBoolean(res);

    }

    public void setPrivateMessagesDisabled(UUID playerUuid, boolean disabled) {
        var redis = pubSubConnection.sync();
        if (disabled) {
            redis.setex("disablePM:" + playerUuid, 60 * 60 * 24, Boolean.toString(true));
        } else {
            redis.del("disablePM:" + playerUuid);
        }


    }

}
