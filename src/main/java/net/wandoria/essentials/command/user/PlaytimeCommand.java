package net.wandoria.essentials.command.user;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.translation.Argument;
import net.wandoria.essentials.EssentialsPlugin;
import net.wandoria.essentials.environment.PluginEnvironment;
import net.wandoria.essentials.user.EssentialsOfflineUser;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.paper.util.sender.PlayerSource;
import org.incendo.cloud.paper.util.sender.Source;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * <p>Own Playtime and another player's playtime </p>
 * <p>Because of @CommandContainer it gets instantiated by {@link net.wandoria.essentials.command.CommandManager}</p>
 */
@CommandContainer
public class PlaytimeCommand {
    private final PluginEnvironment environment = EssentialsPlugin.environment();

    @Command("playtime")
    public CompletableFuture<Void> showOwnPlaytime(PlayerSource playerSource) {
        return environment.getUser(playerSource.source().getUniqueId()).thenAccept((user) -> {
            Duration duration = user.orElseThrow().getPlayTime();
            String humanReadable = DurationFormatUtils.formatDuration(duration.toMillis(), "HH:mm:ss", true);
            playerSource.source().sendMessage(Component.translatable("essentials.playtime.own",
                    Argument.component("playtime", Component.text(humanReadable))
            ));
        });
    }


    @Command("playtime <player>")
    public void showPlaytime(Source source, EssentialsOfflineUser player) {
        Duration duration = player.getPlayTime();
        String humanReadable = DurationFormatUtils.formatDuration(duration.toMillis(), "HH:mm:ss", true);
        source.source().sendMessage(Component.translatable("essentials.playtime.other",
                Argument.component("target", Component.text(player.getName())),
                Argument.component("playtime", Component.text(humanReadable))
        ));
    }
}
