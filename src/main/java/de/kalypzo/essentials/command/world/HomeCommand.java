package de.kalypzo.essentials.command.world;

import de.kalypzo.essentials.EssentialsPlugin;
import de.kalypzo.essentials.command.CommandLoader;
import de.kalypzo.essentials.event.PlayerSetHomeEvent;
import de.kalypzo.essentials.exception.BadConfigurationException;
import de.kalypzo.essentials.gui.home.GuiHomes;
import de.kalypzo.essentials.user.home.Home;
import de.kalypzo.essentials.user.home.HomeManager;
import de.kalypzo.essentials.world.NetworkPosition;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.translation.Argument;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.paper.util.sender.PlayerSource;

import java.util.concurrent.CompletableFuture;

/**
 * Set home and teleport to it.
 * <p>Because of @CommandContainer it gets instantiated by {@link CommandLoader}</p>
 *
 */
@CommandContainer
public class HomeCommand {

    @Command("home|homes")
    @Permission("essentials.command.homes")
    public void openHomeGui(PlayerSource playerSource) {
        try {
            new GuiHomes(playerSource.source(), HomeManager.getInstance()).open();
        } catch (BadConfigurationException e) {
            EssentialsPlugin.instance().getSLF4JLogger().error("Failed to open home GUI for player {}", playerSource.source().getName(), e);
            playerSource.source().sendMessage(Component.translatable("essentials.homes.gui.config-error"));
        }
    }

    @Command("home|homes <home>")
    @Permission("essentials.command.homes.tp")
    @CommandDescription("Teleportiert den Spieler zu seinem Home")
    public void teleportHome(PlayerSource player, Home home) {
        player.source().sendMessage(Component.translatable("essentials.homes.teleport",
                Argument.component("name", Component.text(home.name()))));
        home.teleport(player.source());
    }

    @Command("home|homes set <name>")
    @Command("sethome <name>")
    @Permission("essentials.command.homes.set")
    @CommandDescription("Erstellt ein Home auf der aktuellen Position")
    public CompletableFuture<Void> setHome(PlayerSource playerSource, String name) {
        Player player = playerSource.source();
        var event = new PlayerSetHomeEvent(player);
        if (!event.callEvent()) {
            return CompletableFuture.completedFuture(null);
        }
        final int maxHomes = HomeManager.getInstance().getMaxHomes(player);
        if (maxHomes < 1) {
            player.sendMessage(Component.translatable("essentials.homes.set.max-reached",
                    Argument.numeric("amount", maxHomes)));
            return CompletableFuture.completedFuture(null);
        }
        return HomeManager.getInstance().getHomes(player.getUniqueId()).thenCompose((homes -> HomeManager.getInstance().getHome(player.getUniqueId(), name)
                .thenCompose(existing -> {
                    if (homes.size() >= maxHomes && existing.isEmpty()) {
                        player.sendMessage(Component.translatable("essentials.homes.set.max-reached",
                                Argument.numeric("amount", maxHomes)));
                        return CompletableFuture.completedFuture(null);
                    }
                    return HomeManager.getInstance().saveHome(new Home(
                                    player.getUniqueId(),
                                    name,
                                    NetworkPosition.createByLocation(player.getLocation())))
                            .thenAccept(_void -> {
                                player.sendMessage(Component.translatable("essentials.homes.set.success",
                                        Argument.component("name", Component.text(name))));
                            });
                })));
    }


    @Command("home|homes delete <home>")
    @Command("delhome <home>")
    @Permission("essentials.command.homes.set")
    @CommandDescription("Löscht ein Home")
    public CompletableFuture<Void> deleteHome(PlayerSource player, Home home) {
        return HomeManager.getInstance().deleteHome(home).thenAccept(_void -> {
            player.source().sendMessage(Component.translatable("essentials.homes.delete.success",
                    Argument.component("name", Component.text(home.name()))));
        });
    }


}
