package net.wandoria.essentials.command.admin;

import it.einjojo.playerapi.NetworkPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.wandoria.essentials.chat.ChatMessage;
import net.wandoria.essentials.user.EssentialsUser;
import net.wandoria.essentials.util.Text;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.paper.util.sender.Source;

@CommandContainer
public class SendMessage {

    @Command("sendMiniMessage <receiver> <message>")
    @Permission("essentials.admin.sendminimessage")
    public void sendMiniMessage(Source source, EssentialsUser receiver, String message) {
        source.source().sendActionBar(Component.text("Message delivered", NamedTextColor.GREEN));
        receiver.sendMessage(Text.deserialize(message));
    }


}
