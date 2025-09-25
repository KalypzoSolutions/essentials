package net.wandoria.essentials.user.home;


import net.wandoria.essentials.EssentialsPlugin;
import net.wandoria.essentials.event.AsyncPlayerHomeTeleportEvent;
import net.wandoria.essentials.world.NetworkPosition;
import net.wandoria.essentials.world.TeleportExecutor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * @param owner    who owns the home
 * @param name     the name of the home
 * @param location network location
 */
@NullMarked
public record Home(UUID owner, String name, NetworkPosition location) {
    public static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{1,64}$");

    public Home {
        if (!NAME_PATTERN.matcher(name).matches()) {
            throw new IllegalArgumentException("Invalid home name '" + name + "'");
        }

    }

    /**
     * Teleports the player to the home. Calls {@link AsyncPlayerHomeTeleportEvent} which can be canceled.
     * Gets executed asynchronously. If called from the main thread, it will be scheduled asynchronously.
     *
     * @param player the bukkit player to teleport
     */
    public void teleport(Player player) {
        if (Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTaskAsynchronously(EssentialsPlugin.instance(), () -> teleport(player));
        }
        if (!new AsyncPlayerHomeTeleportEvent(player, this).callEvent()) return;
        TeleportExecutor.getInstance().teleportPlayerToPosition(player.getUniqueId(), location);
    }

}
