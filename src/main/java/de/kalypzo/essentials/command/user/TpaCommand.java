package de.kalypzo.essentials.command.user;

import de.kalypzo.essentials.command.CommandManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.translation.Argument;
import de.kalypzo.essentials.user.EssentialsUser;
import de.kalypzo.essentials.user.tpa.TpaManager;
import de.kalypzo.essentials.util.TagResolvers;
import de.kalypzo.essentials.util.Text;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.paper.util.sender.PlayerSource;

import java.util.concurrent.CompletableFuture;

/**
 * <p>Workbench, Anvil</p>
 * <p>Because of @CommandContainer it gets instantiated by {@link CommandManager}</p>
 */
@CommandContainer
public class TpaCommand {


    @Command("tpa <player>")
    public CompletableFuture<Void> sendTpaRequest(PlayerSource sender, EssentialsUser player) {
        return TpaManager.getInstance().create(sender.source().getUniqueId(), player.getUniqueId())
                .thenAccept(request -> {
                    sender.source().sendMessage(Component.translatable("essentials.tpa.request-sent", Argument.tagResolver(player.playerTagResolver())));
                    Component tpaccept = Component.text("/tpaccept " + sender.source().getName()).clickEvent(ClickEvent.runCommand("/tpaccept " + sender.source().getName()));
                    player.sendMessage(Text.deserialize("<prefix> <p><hl><player></hl> möchte sich zu dir teleportieren.<newline>" +
                                    "<prefix> <p>Nutze: <hl><u><tpacmd>",
                            TagResolvers.player(sender.source()),
                            Placeholder.component("tpacmd", tpaccept)));
                });
    }

    @Command("tpaccept <player>")
    public CompletableFuture<Void> accept(PlayerSource source, EssentialsUser player) {
        if (player.getUniqueId().equals(source.source().getUniqueId())) {
            source.source().sendMessage(Component.translatable("essentials.tpa.not-self"));
            return CompletableFuture.completedFuture(null);
        }
        var requestOpt = TpaManager.getInstance().getRequest(player.getUniqueId(), source.source().getUniqueId());
        if (requestOpt.isEmpty()) {
            source.source().sendMessage(Component.translatable("essentials.tpa.no-request", Argument.tagResolver(player.playerTagResolver())));
            return CompletableFuture.completedFuture(null);
        }
        return requestOpt.get().fulfill()
                .thenAccept((_void) -> {
                    source.source().sendMessage(Component.translatable("essentials.tpa.accept", Argument.tagResolver(player.playerTagResolver())));
                });
    }
}

