package net.wandoria.essentials.command.world;


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.translation.Argument;
import net.wandoria.essentials.EssentialsPlugin;
import net.wandoria.essentials.environment.PluginEnvironment;
import net.wandoria.essentials.user.EssentialsUser;
import net.wandoria.essentials.util.TranslationConstants;
import net.wandoria.essentials.world.NetworkPosition;
import net.wandoria.essentials.world.TeleportExecutor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
 * <p>Because of @CommandContainer it gets instantiated by {@link net.wandoria.essentials.command.CommandManager}</p>
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
    }

    @Command("teleport|tp <target> [destination]")
    @CommandDescription("Teleportiert dich zu einem Spieler oder einem Ort oder teleportiert einen anderen Spieler zu einem Spieler")
    @Permission("essentials.command.teleport")
    public void teleport(Source source, EssentialsUser target, @Nullable EssentialsUser destination) {
        if (destination != null) {
            // Teleportiere den Spieler 1 zu dem Spieler 2
            target.teleport(destination);
            source.source().sendMessage(Component.translatable("essentials.teleport.success",
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
        getTeleportExecutor().teleportPlayerToPlayer(playerSource.source().getUniqueId(), targetUuid);
        source.source().sendMessage(Component.translatable("essentials.teleport.success",
                Argument.component("target", target)
        ));

    }

    public TeleportExecutor getTeleportExecutor() {
        return TeleportExecutor.getInstance();
    }

}
