package net.wandoria.essentials.command.user;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.translation.Argument;
import net.wandoria.essentials.EssentialsPlugin;
import net.wandoria.essentials.rce.RemoteCommandCall;
import net.wandoria.essentials.user.EssentialsUser;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.paper.util.sender.Source;

/**
 *
 * <p>Change gamemode</p>
 * <p>Because of @CommandContainer it gets instantiated by {@link net.wandoria.essentials.command.CommandManager}</p>
 */

@CommandContainer
public class GameModeCommand {

    @Command("gamemode|gm <gamemode> [player]")
    @Permission("essentials.command.gamemode")
    public void gameMode(Source source, GameMode gamemode, final EssentialsUser player) {
        Bukkit.getScheduler().runTask(EssentialsPlugin.instance(), () -> {
            if (player != null) { // other player
                player.ifOnlineLocallyOrElse(bukkit -> {
                    bukkit.setGameMode(gamemode);
                }, () -> {
                    RemoteCommandCall.console(player.getServerName(), "gm " + gamemode.name() + " " + player.getName()).execute();

                });
                source.source().sendMessage(Component.translatable("essentials.command.gamemode.other",
                        Argument.component("target", player),
                        Argument.component("gamemode", Component.text(gamemode.name()))
                ));
            } else { // self
                if (!(source.source() instanceof Player playerSender)) {
                    source.source().sendMessage(Component.translatable("essentials.command.player-only"));
                    return;
                }
                playerSender.setGameMode(gamemode);
                source.source().sendMessage(Component.translatable("essentials.command.gamemode.self",
                        Argument.component("gamemode", Component.text(gamemode.name()))
                ));
            }
        });
    }

}
