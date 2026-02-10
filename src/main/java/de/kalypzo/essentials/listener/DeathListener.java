package de.kalypzo.essentials.listener;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import de.kalypzo.essentials.command.user.BackCommand;
import de.kalypzo.essentials.user.back.BackManager;
import de.kalypzo.essentials.world.NetworkPosition;
import de.kalypzo.essentials.world.warps.Warp;
import de.kalypzo.essentials.world.warps.WarpManager;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;


/**
 * When a player dies, store their location for /back command and teleport them to spawn on respawn.
 */
@Slf4j
public record DeathListener(BackManager backManager) implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void handleDeathEvent(PlayerDeathEvent event) {
        var player = event.getEntity();
        if (!player.hasPermission(BackCommand.PERMISSION)) {
            return;
        }
        NetworkPosition pos = NetworkPosition.createByLocation(event.getPlayer().getLocation());
        backManager.setDeathLocation(player.getUniqueId(), pos);
        log.info("Stored death location for player {}: {}", player.getName(), pos);
        player.sendMessage(Component.translatable("essentials.back.death-location-set"));
    }

    @EventHandler
    public void handleRespawn(PlayerPostRespawnEvent event) {
        WarpManager.getInstance().getWarp("spawn")
                .ifPresent(warp -> warp.teleport(event.getPlayer(), Warp.Reason.DEATH));
    }
}
