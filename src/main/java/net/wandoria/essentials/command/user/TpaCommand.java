package net.wandoria.essentials.command.user;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.wandoria.essentials.user.EssentialsUser;
import net.wandoria.essentials.user.tpa.TpaManager;
import net.wandoria.essentials.util.TagResolvers;
import net.wandoria.essentials.util.Text;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.paper.util.sender.PlayerSource;

import java.util.concurrent.CompletableFuture;

/**
 * <p>Workbench, Anvil</p>
 * <p>Because of @CommandContainer it gets instantiated by {@link net.wandoria.essentials.command.CommandManager}</p>
 */
@CommandContainer
public class TpaCommand {


    @Command("tpa <player>")
    public CompletableFuture<Void> sendTpaRequest(PlayerSource sender, EssentialsUser player) {
        return TpaManager.getInstance().create(sender.source().getUniqueId(), player.getUniqueId())
                .thenAccept(request -> {
                    sender.source().sendMessage(Text.deserialize("<prefix> <p>TPA an <hl><player></hl> gesendet.", player.playerTagResolver()));
                    Component tpaccept = Component.text("/tpaccept " + sender.source().getName()).clickEvent(ClickEvent.runCommand("/tpaccept " + sender.source().getName()));
                    player.sendMessage(Text.deserialize("<prefix> <p><hl><player></hl> möchte sich zu dir teleportieren.<newline>" +
                                    "<prefix> <p>Nutze: <hl><u><tpacmd>",
                            TagResolvers.player(sender.source()),
                            Placeholder.component("tpacmd", tpaccept)));
                });
    }

    @Command("tpaccept <player>")
    public CompletableFuture<Void> accept(PlayerSource source, EssentialsUser player) {
        var requestOpt = TpaManager.getInstance().getRequest(player.getUniqueId(), source.source().getUniqueId());
        if (requestOpt.isEmpty()) {
            source.source().sendMessage(Text.deserialize("<prefix> <p>Keine Teleportanfrage von <player> gefunden", player.playerTagResolver()));
            return CompletableFuture.completedFuture(null);
        }
        return requestOpt.get().fulfill()
                .thenAccept((_void) -> {
                    source.source().sendMessage(Text.deserialize("<prefix> <p>Du hast die Tpa angenommen."));
                });
    }
}

