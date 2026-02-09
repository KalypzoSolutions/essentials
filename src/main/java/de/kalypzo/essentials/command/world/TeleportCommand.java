package de.kalypzo.essentials.command.world;


import de.kalypzo.essentials.command.CommandManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.translation.Argument;
import de.kalypzo.essentials.EssentialsPlugin;
import de.kalypzo.essentials.environment.PluginEnvironment;
import de.kalypzo.essentials.user.EssentialsUser;
import de.kalypzo.essentials.util.TranslationConstants;
import de.kalypzo.essentials.world.NetworkPosition;
import de.kalypzo.essentials.world.TeleportExecutor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.paper.util.sender.PlayerSource;
import org.incendo.cloud.paper.util.sender.Source;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

/**
 * <p>/tpl /tp </p>
 * <p>Because of @CommandContainer it gets instantiated by {@link CommandManager}</p>
 */
@CommandContainer
@NullMarked
public class TeleportCommand {
    private final PluginEnvironment environment = EssentialsPlugin.environment();


    @Command("teleportl|tpl <location> [world] [server]")
    @Permission("essentials.command.teleport")
    @CommandDescription("Teleportiert dich zu einem Ort")
    public void teleport(PlayerSource source, Location location, @Nullable String world, @Nullable String server) {
        if (world == null) {
            world = source.source().getWorld().getName();
        }
        if (server == null) {
            server = environment.getServerName();
        }
        if (server.equals(environment.getServerName()) && Bukkit.getWorld(world) == null) {
            source.source().sendMessage(Component.translatable("essentials.teleport.world-not-found", Component.text(world)));
            return;
        }
        Location current = source.source().getLocation();
        var pos = new NetworkPosition(server, world, location.x(), location.y(), location.z(), current.getYaw(), current.getPitch());
        getTeleportExecutor().teleportPlayerToPosition(source.source().getUniqueId(), pos);
        source.source().playSound(source.source().getLocation(), Sound.ENTITY_PLAYER_TELEPORT, 1, 1.2f);
    }

    @Command("teleport|tp <target> [destination]")
    @CommandDescription("Teleportiert dich zu einem Spieler oder einem Ort oder teleportiert einen anderen Spieler zu einem Spieler")
    @Permission("essentials.command.teleport")
    public void teleport(Source source, EssentialsUser target, @Nullable EssentialsUser destination) {
        if (destination != null) {
            // Teleportiere den Spieler 1 zu dem Spieler 2
            target.teleport(destination);
            source.source().sendMessage(Component.translatable("essentials.teleport.other-success",
                    Argument.component("target", Component.text(target.getName())),
                    Argument.component("destination", Component.text(destination.getName()))
            ));
            return;
        }

        if (!(source instanceof PlayerSource playerSource)) {
            source.source().sendMessage(TranslationConstants.COMMAND_REQUIRES_PLAYER_EXECUTOR);
            return;
        }
        UUID targetUuid = target.getUniqueId();
        source.source().sendMessage(Component.translatable("essentials.teleport.success",
                Argument.component("target", target)
        ));
        getTeleportExecutor().teleportPlayerToPlayer(playerSource.source().getUniqueId(), targetUuid);
    }

    public TeleportExecutor getTeleportExecutor() {
        return TeleportExecutor.getInstance();
    }

}
