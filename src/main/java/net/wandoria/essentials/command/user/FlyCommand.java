package net.wandoria.essentials.command.user;

import net.kyori.adventure.text.Component;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.paper.util.sender.PlayerSource;

/**
 * <p>Because of @CommandContainer it gets instantiated by {@link net.wandoria.essentials.command.CommandManager}</p>
 */
@CommandContainer
public class FlyCommand {

    @Command("fly")
    @Permission("essentials.command.fly")
    @CommandDescription("Aktiviert den Flugmodus")
    public void fly(PlayerSource source) {
        if (source.source().isFlying()) {
            source.source().setFlying(false);
            source.source().setAllowFlight(false);
            source.source().sendMessage(Component.translatable("essentials.fly.disabled"));

        } else {
            source.source().setAllowFlight(true);
            source.source().setFlying(true);
            source.source().sendMessage(Component.translatable("essentials.fly.enabled"));
        }
    }


}
