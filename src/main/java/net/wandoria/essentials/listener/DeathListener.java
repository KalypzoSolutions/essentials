package net.wandoria.essentials.listener;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.wandoria.essentials.command.user.BackCommand;
import net.wandoria.essentials.user.back.BackManager;
import net.wandoria.essentials.world.NetworkPosition;
import net.wandoria.essentials.world.warps.WarpManager;
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
        WarpManager.getInstance().getWarp("spawn").ifPresent(warp -> warp.teleport(event.getPlayer()));
    }
}
