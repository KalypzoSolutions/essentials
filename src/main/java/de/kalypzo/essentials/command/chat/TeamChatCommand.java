package de.kalypzo.essentials.command.chat;

import de.kalypzo.essentials.chat.ChatMessage;
import de.kalypzo.essentials.command.CommandLoader;
import de.kalypzo.essentials.util.Text;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.Node;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.paper.util.sender.PlayerSource;

import java.util.concurrent.CompletableFuture;

/**
 * <p>Provides all commands related to private messages / reply </p>
 * <p>Because of @CommandContainer it gets instantiated by {@link CommandLoader}</p>
 */
@CommandContainer
public class TeamChatCommand {
    public static final String SCOPE = "essentials.teamchat.receive";
    public static Node TEAMCHAT_TOGGLED_NODE = Node.builder("essentialsmeta.teamchat.toggled").build();
    public static Node TEAMCHAT_SILENT_NODE = Node.builder("essentialsmeta.teamchat.silent").build();

    @Command("teamchat|tc <message>")
    @Permission("essentials.command.teamchat")
    public CompletableFuture<Void> reply(PlayerSource sender, @Greedy String message) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        Component output = Text.deserialize(PlaceholderAPI.setPlaceholders(sender.source(), "<#a855f7>[<b>ᴛᴄ</b>] <white>%luckperms_prefix% %player_name%</white> <#f4f1de>→ <#a855f7><message>"),
                Placeholder.component("message", Component.text(message))
        );
        ChatMessage.createPermissionScoped(output, SCOPE).deliver();
        return future;
    }

    @Command("teamchat|tc toggle")
    @Permission("essentials.command.teamchat.toggle")
    public void toggle(PlayerSource sender) {
        Player player = sender.source();
        if (player.hasPermission("essentialsmeta.teamchat.toggled")) {
            sender.source().sendMessage(Text.deserialize("<prefix> Alle Nachrichten werden nun in den Chat gesendet."));
            LuckPermsProvider.get().getUserManager().modifyUser(player.getUniqueId(), user -> {
                user.data().remove(TEAMCHAT_TOGGLED_NODE);
            });
        } else {
            sender.source().sendMessage(Text.deserialize("<prefix> Alle Nachrichten werden nun im <#bdb2ff>TeamChat</<#bdb2ff> gesendet."));
            LuckPermsProvider.get().getUserManager().modifyUser(player.getUniqueId(), user -> {
                user.data().add(TEAMCHAT_TOGGLED_NODE);
            });
        }

    }

    @Command("teamchat|tc silent")
    @Permission("essentials.command.silent")
    public void silent(PlayerSource sender) {
        Player player = sender.source();
        if (player.hasPermission("essentialsmeta.teamchat.silent")) {
            sender.source().sendMessage(Text.deserialize("<prefix> Du siehst wieder Team-Chat Nachrichten."));
            LuckPermsProvider.get().getUserManager().modifyUser(player.getUniqueId(), user -> {
                user.data().remove(TEAMCHAT_SILENT_NODE);
            });

        } else {
            sender.source().sendMessage(Text.deserialize("<prefix> Du hast den Teamchat stummgeschaltet."));
            LuckPermsProvider.get().getUserManager().modifyUser(player.getUniqueId(), user -> {
                user.data().add(TEAMCHAT_SILENT_NODE);
            });
        }
    }
}
