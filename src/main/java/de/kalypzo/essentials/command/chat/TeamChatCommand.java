package de.kalypzo.essentials.command.chat;

import de.kalypzo.essentials.chat.ChatMessage;
import de.kalypzo.essentials.chat.TeamChatService;
import de.kalypzo.essentials.util.Text;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import studio.mevera.imperat.annotations.types.Execute;
import studio.mevera.imperat.annotations.types.Greedy;
import studio.mevera.imperat.annotations.types.Permission;
import studio.mevera.imperat.annotations.types.RootCommand;
import studio.mevera.imperat.annotations.types.SubCommand;

@RootCommand({"teamchat", "tc"})
public class TeamChatCommand {
    public static final String SCOPE = "essentials.teamchat.receive";
    public static Node TEAMCHAT_TOGGLED_NODE = Node.builder("essentialsmeta.teamchat.toggled").build();
    public static Node TEAMCHAT_SILENT_NODE = Node.builder("essentialsmeta.teamchat.silent").build();
    private static TeamChatService service;


    public static void setTeamChatService(TeamChatService teamChatService) {
        service = teamChatService;
    }

    @Execute
    @Permission("essentials.command.teamchat")
    public void reply(Player sender, @Greedy String message) {
        Component output = Text.deserialize(PlaceholderAPI.setPlaceholders(sender, "<#a855f7>[<b>ᴛᴄ</b>] <white>%luckperms_prefix% %player_name%</white> <#f4f1de>→ <#a855f7><message>"),
                Placeholder.component("message", Component.text(message))
        );
        ChatMessage.createPermissionScoped(output, SCOPE).deliver();
    }

    @SubCommand("toggle")
    @Permission("essentials.command.teamchat.toggle")
    public void toggle(Player sender) {

        boolean state =
                !service.isToggled(sender.getUniqueId());

        service.setToggled(sender.getUniqueId(), state);

        sender.sendMessage(Text.deserialize(
                state
                        ? "<prefix> <#bdb2ff>TeamChat aktiviert."
                        : "<prefix> Öffentlicher Chat aktiviert."
        ));
    }

    @SubCommand("silent")
    @Permission("essentials.command.silent")
    public void silent(Player sender) {

        boolean state =
                !service.isSilent(sender.getUniqueId());

        service.setSilent(sender.getUniqueId(), state);

        sender.sendMessage(Text.deserialize(
                state
                        ? "<prefix> <red>Du hast den Teamchat stummgeschaltet."
                        : "<prefix> <green>Du siehst nun wieder Teamchat-Nachrichten."
        ));
    }
}
