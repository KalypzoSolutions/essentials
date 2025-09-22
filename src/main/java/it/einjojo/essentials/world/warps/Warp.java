package it.einjojo.essentials.world.warps;


import it.einjojo.essentials.EssentialsPlugin;
import it.einjojo.essentials.event.AsyncPlayerWarpTeleportEvent;
import it.einjojo.essentials.world.NetworkPosition;
import it.einjojo.essentials.world.TeleportExecutor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public record Warp(String name, NetworkPosition location) {

    public void teleport(Player player) {
        if (Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTaskAsynchronously(EssentialsPlugin.instance(), () -> teleport(player));
            return;
        }
        if (!new AsyncPlayerWarpTeleportEvent(player, this).callEvent()) {
            return;
        }
        player.sendMessage(Component.translatable("essentials.warp.teleporting", Component.text(name)));
        TeleportExecutor.getInstance().teleportPlayerToPosition(player.getUniqueId(), location);
    }

}
