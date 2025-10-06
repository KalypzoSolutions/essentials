package net.wandoria.essentials.user.back;

import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.wandoria.essentials.world.NetworkPosition;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;


/**
 * When a player dies, store their location for /back command.
 */
@Slf4j
public record DeathListener(BackManager backManager) implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void handleDeathEvent(PlayerDeathEvent event) {
        var player = event.getEntity();
        NetworkPosition pos = NetworkPosition.createByLocation(event.getPlayer().getLocation());
        backManager.setDeathLocation(player.getUniqueId(), pos);
        log.info("Stored death location for player {}: {}", player.getName(), pos);
        player.sendMessage(Component.translatable("essentials.back.death-location-set"));
    }
}
