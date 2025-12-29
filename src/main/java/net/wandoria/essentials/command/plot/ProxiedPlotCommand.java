package net.wandoria.essentials.command.plot;

import net.kyori.adventure.text.Component;
import net.wandoria.essentials.EssentialsPlugin;
import net.wandoria.essentials.environment.PluginEnvironment;
import net.wandoria.essentials.rce.RemoteCommandCall;
import net.wandoria.essentials.user.EssentialsOfflineUser;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.paper.util.sender.PlayerSource;
import org.incendo.cloud.paper.util.sender.Source;
import org.jspecify.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 *
 */
public class ProxiedPlotCommand {
    private final String serverName;
    private final PluginEnvironment environment;

    public ProxiedPlotCommand(String serverName) {
        this.serverName = serverName;
        this.environment = EssentialsPlugin.environment();
    }


    @Command("plot|p auto")
    @Command("plot|p home")
    @Command("plot|p h")
    public CompletableFuture<Void> proxy(PlayerSource source, CommandContext<Source> ctx) {
        return proxyCommand(source.source().getUniqueId(), ctx.rawInput().input(), source.source());
    }

    @Command("plot|p h visit <player> [plot]")
    public CompletableFuture<Void> proxyVisit(PlayerSource source, CommandContext<Source> ctx, EssentialsOfflineUser player, @Nullable Integer plot) {
        return proxyCommand(source.source().getUniqueId(), ctx.rawInput().input(), source.source());
    }


    private CompletableFuture<Void> proxyCommand(UUID playerId, String command, CommandSender sender) {
        return environment.connectPlayerToServer(playerId, serverName).thenAccept((success) -> {
            if (!success) {
                sender.sendMessage(Component.translatable("essentials.plot.connect.failed"));
                return;
            }
            RemoteCommandCall.player(playerId, serverName, command);
        });
    }

}
