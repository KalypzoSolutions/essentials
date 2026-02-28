package de.kalypzo.essentials.command.user;

import de.kalypzo.essentials.command.CommandManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.translation.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.paper.util.sender.PlayerSource;

/**
 * Set flight or normal walking speed for the player.
 * <p>Speed range: 0 to 10 (normalized to 0.0-1.0 for Minecraft)</p>
 * <p>Because of @CommandContainer it gets instantiated by {@link CommandManager}</p>
 */
@CommandContainer
public class SpeedCommand {

    /**
     * Sets the player's flight speed or walking speed.
     * If the player is flying, sets flight speed; otherwise sets walking speed.
     * Accepts speed values from 0 to 10 and normalizes them to Minecraft's expected range (0.0-1.0).
     *
     * @param source the player executing the command
     * @param speed the desired speed value (0 - 10)
     */
    @Command("speed <speed>")
    @Permission("essentials.command.speed")
    @CommandDescription("Setzt deine Fluggeschwindigkeit oder Laufgeschwindigkeit")
    public void speed(PlayerSource source, float speed) {
        // Clamp input speed between 0 and 10
        float clampedInput = Math.max(0.0f, Math.min(10.0f, speed));

        // Normalize to Minecraft's expected range (0.0-1.0)
        float normalizedSpeed = clampedInput / 10.0f;

        if (source.source().isFlying()) {
            source.source().setFlySpeed(normalizedSpeed);
            source.source().sendMessage(Component.translatable("essentials.speed.flight.set",
                    Argument.component("speed", Component.text(String.format("%.1f", clampedInput)))
            ));
        } else {
            source.source().setWalkSpeed(normalizedSpeed);
            source.source().sendMessage(Component.translatable("essentials.speed.walk.set",
                    Argument.component("speed", Component.text(String.format("%.1f", clampedInput)))
            ));
        }
    }
}


