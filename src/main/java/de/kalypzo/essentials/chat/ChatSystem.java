package de.kalypzo.essentials.chat;

import com.google.gson.Gson;
import de.kalypzo.essentials.EssentialsPlugin;
import de.kalypzo.essentials.user.EssentialsUser;
import de.kalypzo.essentials.user.UserSettings;
import de.kalypzo.essentials.util.Text;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ChatSystem is responsible for two things: Private Messages and Cross-Server Messages.
 * <p>Redis pub sub is used for cross-server messages, and the last private message sender is stored in redis with  </p>
 */
@Getter
@Slf4j
public class ChatSystem implements Listener {
    private static final Permission COLORED_CHAT_PERMISSION = new Permission("essentials.chat.colored");
    private final Gson gson = new Gson();
    private static final int TTL_SECONDS = 60 * 30;
    private static final String LAST_PRIVATE_MSG_KEY_PREFIX = "lastPM:";
    private final PlainTextComponentSerializer plain = PlainTextComponentSerializer.plainText();
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final StatefulRedisPubSubConnection<String, String> pubSubConnection;
    private final ChatConfiguration chatConfiguration;
    private final String serverName;
    private final MiniMessage COLOR_ONLY = MiniMessage.builder().tags(
                    TagResolver.builder()
                            .resolver(StandardTags.color())
                            .resolver(StandardTags.decorations())
                            .build())
            .build();

    /**
     * Thread-safe cache for player alias sets.
     * Maps player UUID -> PlayerNameAliasSet for efficient mention detection.
     * ConcurrentHashMap ensures thread-safe access without explicit synchronization.
     * Explicitly cleaned up on player quit to prevent memory leaks.
     */
    private final Map<UUID, PlayerNameAliasSet> playerAliasCache = new ConcurrentHashMap<>();


    /**
     * <p>Create a instance of the ChatSystem.</p>
     * <p>Will subscribe to the redis-pubsub 'chat' channel.</p>
     * <p>Will register a chat listener</p>
     * <p>The PlaceholderAPI is required to be loaded</p>
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
        Bukkit.getPluginManager().addPermission(COLORED_CHAT_PERMISSION);
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


    /**
     * Cancels every message which has not been canceled before and will publish the message to redis.
     *
     * @param event the event which is being canceled.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncChatEvent event) {
        if (event.isCancelled()) {
            return;
        }
        try {
            String chatFormat = chatConfiguration.getChatFormat();
            chatFormat = PlaceholderAPI.setPlaceholders(event.getPlayer(), chatFormat);
            if (event.getPlayer().hasPermission(COLORED_CHAT_PERMISSION)) {
                event.message(COLOR_ONLY.deserialize(Text.replaceLegacyColorCodesWithMiniMessage(plain.serialize(event.message()))));
            }
            Component formattedMessage = miniMessage.deserialize(chatFormat,
                    Placeholder.component("message", event.message()),
                    Placeholder.unparsed("server", serverName),
                    Placeholder.unparsed("time", LocalTime.now().format(TIME_FORMATTER))
            );
            publishNetworkChatMessage(ChatMessage.create(formattedMessage, event.getPlayer().getUniqueId(), serverName)).exceptionally((ex) -> {
                handleChatException(event, ex);
                return null;
            });
        } catch (Exception ex) {
            handleChatException(event, ex);
        }
        event.setCancelled(true);
    }


    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerAliasCache.remove(event.getPlayer().getUniqueId());
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
        if (chatMessage.recipients() == null && chatMessage.permissionScope() == null) {
            recipients = new ArrayList<>(Bukkit.getOnlinePlayers());
        } else if (chatMessage.recipients() == null) { // permission Scope is given
            recipients = new LinkedList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission(chatMessage.permissionScope())) {
                    recipients.add(player);
                }
            }
        } else {
            recipients = new LinkedList<>();
            for (UUID target : chatMessage.recipients()) {
                Player player = Bukkit.getPlayer(target);
                if (player != null) {
                    recipients.add(player);
                }
            }
        }
        Component content = chatMessage.getContent();
        String serializedMessage = chatMessage.serializedMessage();
        String plainText = plain.serialize(content);  // Plain text version for regex matching
        UUID senderUuid = chatMessage.sender();
        String originatingServer = chatMessage.originatingServer();

        // Check if any player is mentioned and prepare highlighted message
        Component highlightedMessage = null;

        for (Player recipient : recipients) {
            if (isMentioned(serializedMessage, recipient)) {
                highlightedMessage = highlightMentionedName(content, recipient, plainText);
                break;
            }
        }

        // Handle sender separately - only on the originating server
        // This prevents duplicate messages when message is broadcast to all servers
        if (senderUuid != null && serverName.equals(originatingServer)) {
            Player sender = Bukkit.getPlayer(senderUuid);
            if (sender != null) {
                // Sender sees highlighted version if someone is mentioned, otherwise normal message
                // No ping sound for sender (they initiated the message)
                sender.sendMessage(highlightedMessage != null ? highlightedMessage : content);
            }
        }

        for (Player recipient : recipients) {
            // Skip sender in global chat rendering (they already received their message above)
            if (senderUuid != null && recipient.getUniqueId().equals(senderUuid)) {
                continue;
            }

            // Check if recipient is mentioned using efficient alias set lookup
            if (isMentioned(serializedMessage, recipient)) {
                Component pingedMessage = highlightMentionedName(content, recipient, plainText);
                recipient.sendMessage(pingedMessage);
                if (!UserSettings.of(recipient.getUniqueId()).disabledPingSound()) {
                    playPingSound(recipient);
                }
            } else {
                recipient.sendMessage(content);
            }
        }
    }

    /**
     * Checks if a player is mentioned in the message using efficient regex pattern matching.
     * Uses precompiled alias patterns for O(m) performance (m = message length).
     *
     * @param serializedMessage the message text to search in
     * @param player            the player to check for mentions
     * @return true if any alias of the player is mentioned with '@' prefix
     */
    private boolean isMentioned(@NotNull String serializedMessage, @NotNull Player player) {
        PlayerNameAliasSet aliases = getOrCreateAliasSet(player);
        return aliases.isMentioned(serializedMessage);
    }

    /**
     * Lazily retrieves or creates the alias set for a player.
     * Uses weak reference caching to allow garbage collection when player goes offline.
     *
     * @param player the player to get aliases for
     * @return the cached or newly created alias set
     */
    private PlayerNameAliasSet getOrCreateAliasSet(@NotNull Player player) {
        return playerAliasCache.computeIfAbsent(player.getUniqueId(), uuid -> new PlayerNameAliasSet(player));
    }

    /**
     * Highlights the player's name in a message by finding the matched alias.
     * Uses efficient regex matching to locate which alias was used.
     *
     * @param content   the message component to highlight
     * @param player    the player whose mention should be highlighted
     * @param plainText the plain text version of the message (for regex matching)
     * @return the component with highlighted mentions
     */
    private Component highlightMentionedName(@NotNull Component content, @NotNull Player player, @NotNull String plainText) {
        PlayerNameAliasSet aliases = getOrCreateAliasSet(player);
        String playerName = player.getName();

        // Find which alias was used (efficient regex match)
        String matchedAlias = aliases.getFirstMention(plainText);
        if (matchedAlias != null) {
            String mention = "@" + matchedAlias;
            return content.replaceText(builder -> builder.match(mention)
                    .replaceInsideHoverEvents(false)
                    .replacement(Component.text("@" + playerName).color(Text.getHighlightColor())));
        }

        // Fallback: highlight with actual player name (shouldn't happen)
        return content.replaceText(builder -> builder.match("@" + playerName)
                .replaceInsideHoverEvents(false)
                .replacement(Component.text("@" + playerName).color(Text.getHighlightColor())));
    }

    public void playPingSound(Player player) {
        player.playSound(player, chatConfiguration.getPingSound(), 1, 1.4f);
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
