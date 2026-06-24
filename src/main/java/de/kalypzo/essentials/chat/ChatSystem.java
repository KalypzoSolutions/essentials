package de.kalypzo.essentials.chat;

import com.google.gson.Gson;
import de.kalypzo.essentials.EssentialsPlugin;
import de.kalypzo.essentials.chat.emoji.Emoji;
import de.kalypzo.essentials.chat.emoji.EmojiGui;
import de.kalypzo.essentials.chat.emoji.EmojiRegistry;
import de.kalypzo.essentials.command.chat.TeamChatCommand;
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
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
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

    private final Map<UUID, PlayerNameAliasSet> playerAliasCache = new ConcurrentHashMap<>();

    private final TeamChatService teamChatService;

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

        this.teamChatService = new TeamChatService(pubSubConnection.sync());

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

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        teamChatService.loadPlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        playerAliasCache.remove(uuid);
        teamChatService.unloadPlayer(uuid);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncChatEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        try {
            boolean isTeamChat = teamChatService.isToggled(player.getUniqueId());
            String chatFormat = isTeamChat ? chatConfiguration.getTeamChatFormat() : chatConfiguration.getChatFormat();
            chatFormat = PlaceholderAPI.setPlaceholders(player, chatFormat);

            // Emoji-Shortcuts ersetzen
            String plainMsg = plain.serialize(event.message());
            plainMsg = replaceEmojiShortcuts(player, plainMsg);
            event.message(Component.text(plainMsg));

            if (player.hasPermission(COLORED_CHAT_PERMISSION)) {
                event.message(COLOR_ONLY.deserialize(Text.replaceLegacyColorCodesWithMiniMessage(plain.serialize(event.message()))));
            }
            Component formattedMessage = miniMessage.deserialize(chatFormat,
                    Placeholder.component("message", event.message()),
                    Placeholder.unparsed("server", serverName),
                    Placeholder.unparsed("time", LocalTime.now().format(TIME_FORMATTER))
            );
            String scope = isTeamChat ? TeamChatCommand.SCOPE : null;
            publishNetworkChatMessage(
                    ChatMessage.create(formattedMessage, player.getUniqueId(), serverName, scope)
            ).exceptionally((ex) -> {
                handleChatException(event, ex);
                return null;
            });
        } catch (Exception ex) {
            handleChatException(event, ex);
        }
        event.setCancelled(true);
    }

    public void sendEmojiChat(Player player, String emojiCharacter) {
        Component emojiComponent = Component.text(emojiCharacter);
        ChatMessage message = ChatMessage.create(emojiComponent, player.getUniqueId(), serverName);
        publishNetworkChatMessage(message);
    }

    private String replaceEmojiShortcuts(Player player, String message) {
        EmojiRegistry registry = EssentialsPlugin.instance().getEmojiRegistry();
        if (registry == null) return message;
        for (Emoji emoji : registry.getAll()) {
            if (emoji.hasPermission(player)) {
                message = message.replace(":" + emoji.shortcut() + ":", emoji.character());
            }
        }
        return message;
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
        } else if (chatMessage.recipients() == null) {
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

        boolean isTeamChat =
                TeamChatCommand.SCOPE.equalsIgnoreCase(
                        chatMessage.permissionScope());

        if (isTeamChat) {
            recipients.removeIf(recipient ->
                    !recipient.getUniqueId().equals(chatMessage.sender())
                            && teamChatService.isSilent(recipient.getUniqueId()));
        }

        Component content = chatMessage.getContent();
        String serializedMessage = chatMessage.serializedMessage();
        String plainText = plain.serialize(content);
        UUID senderUuid = chatMessage.sender();
        String originatingServer = chatMessage.originatingServer();

        Component highlightedMessage = null;
        for (Player recipient : recipients) {
            if (isMentioned(serializedMessage, recipient)) {
                highlightedMessage = highlightMentionedName(content, recipient, plainText);
                break;
            }
        }

        if (senderUuid != null && serverName.equals(originatingServer)) {
            Player sender = Bukkit.getPlayer(senderUuid);
            if (sender != null) {
                sender.sendMessage(highlightedMessage != null ? highlightedMessage : content);
            }
        }

        for (Player recipient : recipients) {
            if (senderUuid != null && recipient.getUniqueId().equals(senderUuid)) {
                continue;
            }
            if (isTeamChat) {
                recipient.playSound(recipient, Sound.UI_TOAST_IN, 1, 1.85f);
            }
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
