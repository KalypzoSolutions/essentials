package net.wandoria.essentials.listener;

import net.kyori.adventure.text.Component;
import net.wandoria.essentials.util.InternalServerName;
import net.wandoria.essentials.world.warps.Warp;
import net.wandoria.essentials.world.warps.WarpManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.util.Locale;

/**
 * Overwrites the spawn location of a player to be the spawn
 */
public class JoinSpawnLocationListener implements Listener {
    private Warp spawn;
    private Boolean isSpawnServer;


    @EventHandler
    public void spawnChoose(PlayerSpawnLocationEvent event) {
        if (spawn == null) {
            spawn = WarpManager.getInstance().getWarp("spawn").orElse(null);
            if (spawn == null) {
                event.getPlayer().sendMessage(Component.translatable("essentials.warp.not-found", Component.text("spawn")));
                return;
            }
            isSpawnServer = spawn.location().serverName().equals(InternalServerName.get());
        }
        if (isSpawnServer != null && isSpawnServer) {
            event.setSpawnLocation(spawn.location().toLocation());
        }
    }


}
