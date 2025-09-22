package it.einjojo.essentials.user.home;


import it.einjojo.essentials.event.AsyncPlayerHomeTeleportEvent;
import it.einjojo.essentials.world.NetworkPosition;
import it.einjojo.essentials.world.TeleportExecutor;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * @param owner    who owns the home
 * @param name     the name of the home
 * @param location network location
 */
public record Home(UUID owner, String name, NetworkPosition location) {

    public void teleport(Player player) {
        if (!new AsyncPlayerHomeTeleportEvent(player, this).callEvent()) return;
        TeleportExecutor.getInstance().teleportPlayerToPosition(player.getUniqueId(), location);
    }

}
