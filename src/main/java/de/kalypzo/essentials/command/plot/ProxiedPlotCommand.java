package de.kalypzo.essentials.command.plot;

import net.kyori.adventure.text.Component;
import de.kalypzo.essentials.EssentialsPlugin;
import de.kalypzo.essentials.environment.PluginEnvironment;
import de.kalypzo.essentials.rce.RemoteCommandCall;
import de.kalypzo.essentials.user.EssentialsOfflineUser;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.paper.util.sender.PlayerSource;
import org.incendo.cloud.paper.util.sender.Source;
import org.jspecify.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Allows to run plot-squared commands on a different server.
 * <p>
 * Registered conditionally if Plot-Squared is not present and proxying is enabled in the config. Proxies plot commands to a different server, where the plot plugin is installed.
 */
public class ProxiedPlotCommand {
    private final String serverName;
    private final PluginEnvironment environment;

    public ProxiedPlotCommand(String serverName) {
        this.serverName = serverName;
        this.environment = EssentialsPlugin.environment();
    }

    @Command("plot|p a")
    @Command("plot|p auto")
    public CompletableFuture<Void> proxy(PlayerSource source, CommandContext<Source> ctx) {
        return proxyCommand(source.source().getUniqueId(), ctx.rawInput().input(), source.source());
    }

    @Command("plot|p home [plot]")
    @Command("plot|p h [plot]")
    public CompletableFuture<Void> proxyHome(PlayerSource source, CommandContext<Source> ctx, @Nullable Integer plot) {
        return proxyCommand(source.source().getUniqueId(), ctx.rawInput().input(), source.source());
    }

    @Command("plot|p visit <player> [plot]")
    @Command("plot|p v <player> [plot]")
    public CompletableFuture<Void> proxyVisit(PlayerSource source, CommandContext<Source> ctx, EssentialsOfflineUser player, @Nullable Integer plot) {
        return proxyCommand(source.source().getUniqueId(), ctx.rawInput().input(), source.source());
    }


    private CompletableFuture<Void> proxyCommand(UUID playerId, String command, CommandSender sender) {
        return environment.connectPlayerToServer(playerId, serverName).thenAccept((success) -> {
            if (!success) {
                sender.sendMessage(Component.translatable("essentials.plot.connect.failed"));
                return;
            }
            RemoteCommandCall.player(playerId, serverName, command).executeNow();
        });
    }

}
