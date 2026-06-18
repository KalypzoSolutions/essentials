package de.kalypzo.essentials.command.user;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import studio.mevera.imperat.annotations.types.Description;
import studio.mevera.imperat.annotations.types.Execute;
import studio.mevera.imperat.annotations.types.Permission;
import studio.mevera.imperat.annotations.types.RootCommand;

@RootCommand("fly")
@Permission("essentials.command.fly")
@Description("Aktiviert den Flugmodus")
public class FlyCommand {

    @Execute
    public void fly(Player source) {
        if (source.getAllowFlight()) {
            source.setFlying(false);
            source.setAllowFlight(false);
            source.sendMessage(Component.translatable("essentials.fly.disabled"));
        } else {
            source.setAllowFlight(true);
            source.setFlying(true);
            source.sendMessage(Component.translatable("essentials.fly.enabled"));
        }
    }
}
