package net.wandoria.essentials.command.world;

import net.kyori.adventure.text.Component;
import net.wandoria.essentials.event.PlayerSetHomeEvent;
import net.wandoria.essentials.user.home.Home;
import net.wandoria.essentials.user.home.HomeManager;
import net.wandoria.essentials.world.NetworkPosition;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.paper.util.sender.PlayerSource;

import java.util.concurrent.CompletableFuture;

/**
 * Set home and teleport to it.
 * <p>Because of @CommandContainer it gets instantiated by {@link net.wandoria.essentials.command.CommandManager}</p>
 *
 */
@CommandContainer
public class HomeCommand {

    @Command("home|homes tp <home>")
    @Permission("homes.tp")
    @CommandDescription("Teleportiert den Spieler zu seinem Home")
    public void teleportHome(PlayerSource player, Home home) {
        player.source().sendMessage(Component.translatable("homes.teleport", Component.text(home.name())));
        home.teleport(player.source());
    }

    @Command("home|homes set <name>")
    @Permission("homes.set")
    @CommandDescription("Erstellt ein Home auf der aktuellen Position")
    public CompletableFuture<Void> setHome(PlayerSource playerSource, String name) {
        Player player = playerSource.source();
        var event = new PlayerSetHomeEvent(player);
        if (!event.callEvent()) {
            return CompletableFuture.completedFuture(null);
        }
        int maxHomes = HomeManager.getInstance().getMaxHomes(player);
        if (maxHomes < 1) {
            player.sendMessage(Component.translatable("homes.set.max-reached", Component.text(maxHomes)));
            return CompletableFuture.completedFuture(null);
        }
        return HomeManager.getInstance().getHomes(player.getUniqueId()).thenCompose((homes -> {
            if (homes.size() >= maxHomes) {
                player.sendMessage(Component.translatable("homes.set.max-reached", Component.text(maxHomes)));
                return CompletableFuture.completedFuture(null);
            }
            return HomeManager.getInstance().saveHome(new Home(
                    player.getUniqueId(),
                    name,
                    NetworkPosition.createByLocation(player.getLocation()))).thenAccept(_void -> {
                player.sendMessage(Component.translatable("homes.set.success", Component.text(name)));
            });
        }));

    }


}
