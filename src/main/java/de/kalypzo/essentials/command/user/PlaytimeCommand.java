package de.kalypzo.essentials.command.user;

import de.kalypzo.essentials.EssentialsPlugin;
import de.kalypzo.essentials.environment.PluginEnvironment;
import de.kalypzo.essentials.user.EssentialsOfflineUser;
import de.kalypzo.essentials.user.leaderboard.PlaytimeTopPostgresAccessor;
import de.kalypzo.essentials.util.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.translation.Argument;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.entity.Player;
import studio.mevera.imperat.BukkitCommandSource;
import studio.mevera.imperat.annotations.types.Execute;
import studio.mevera.imperat.annotations.types.Optional;
import studio.mevera.imperat.annotations.types.RootCommand;
import studio.mevera.imperat.annotations.types.SubCommand;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

@RootCommand("playtime")
public class PlaytimeCommand {
    private final PluginEnvironment environment = EssentialsPlugin.environment();
    private final PlaytimeTopPostgresAccessor playtimeTopAccessor = new PlaytimeTopPostgresAccessor(EssentialsPlugin.instance().getDataSource());

    @Execute
    public CompletableFuture<Void> showPlaytime(BukkitCommandSource source, @Optional EssentialsOfflineUser player) {
        if (player != null) {
            Duration duration = player.getPlayTime();
            String humanReadable = DurationFormatUtils.formatDuration(duration.toMillis(), "HH:mm:ss", true);
            source.origin().sendMessage(Component.translatable("essentials.playtime.other",
                    Argument.component("target", Component.text(player.getName())),
                    Argument.component("playtime", Component.text(humanReadable))
            ));
            return CompletableFuture.completedFuture(null);
        }
        if (source.isConsole()) {
            source.origin().sendMessage(Component.translatable("essentials.command.player-only"));
            return CompletableFuture.completedFuture(null);
        }
        Player playerSender = source.asPlayer();
        return environment.getUser(playerSender.getUniqueId()).thenAccept(user -> {
            Duration duration = user.orElseThrow().getPlayTime();
            String humanReadable = DurationFormatUtils.formatDuration(duration.toMillis(), "HH:mm:ss", true);
            playerSender.sendMessage(Component.translatable("essentials.playtime.own",
                    Argument.component("playtime", Component.text(humanReadable))
            ));
        });
    }

    @SubCommand("top")
    public CompletableFuture<Void> showTop(BukkitCommandSource source) {
        refreshPlaytimeStatsIfNeeded();
        if (playtimeTopAccessor.getUpdateFuture() == null || playtimeTopAccessor.getUpdateFuture().isDone()) {
            return showPlaytimeTop(source);
        } else {
            return playtimeTopAccessor.getUpdateFuture().thenCompose(v -> showPlaytimeTop(source));
        }
    }

    @SubCommand("total")
    public CompletableFuture<Void> showTotal(BukkitCommandSource source) {
        refreshPlaytimeStatsIfNeeded();
        if (playtimeTopAccessor.getUpdateFuture() == null || playtimeTopAccessor.getUpdateFuture().isDone()) {
            return showPlaytimeTotal(source);
        } else {
            return playtimeTopAccessor.getUpdateFuture().thenCompose(v -> showPlaytimeTotal(source));
        }
    }

    private CompletableFuture<Void> showPlaytimeTop(BukkitCommandSource source) {
        PlaytimeTopPostgresAccessor.PlaytimeTopEntry[] topTen = playtimeTopAccessor.getTopTen();
        source.origin().sendRichMessage("<gray>Die 10 aktivsten Spieler sind");

        for (int i = 0; i < topTen.length; i++) {
            PlaytimeTopPostgresAccessor.PlaytimeTopEntry entry = topTen[i];
            String pos = String.valueOf(i + 1);
            String name = "-";
            String playtime = "-";
            if (entry != null) {
                name = entry.name();
                playtime = formatPlaytime(entry.playtime());
            }
            source.origin().sendRichMessage(
                    "<dark_gray>◆ <gray><pos> <#b9f8cf><name> <yellow><playtime>",
                    Placeholder.unparsed("pos", pos),
                    Placeholder.unparsed("name", name),
                    Placeholder.unparsed("playtime", playtime)
            );
        }

        return CompletableFuture.completedFuture(null);
    }

    private void refreshPlaytimeStatsIfNeeded() {
        boolean canRefresh = playtimeTopAccessor.getLastUpdate().plusMillis(1000 * 30).isBefore(Instant.now());
        if (canRefresh) {
            playtimeTopAccessor.refreshTopTenAsync();
        }
    }

    private CompletableFuture<Void> showPlaytimeTotal(BukkitCommandSource source) {
        source.origin().sendMessage(Text.deserialize("<prefix> <p>Alle Spieler haben zusammen <hl><playtime></hl> gespielt.",
                Placeholder.unparsed("playtime", formatLongPlaytime(playtimeTopAccessor.getTotalPlaytime()))));
        return CompletableFuture.completedFuture(null);
    }

    private String formatPlaytime(Duration duration) {
        return DurationFormatUtils.formatDuration(duration.toMillis(), "HH:mm:ss", true);
    }

    private String formatLongPlaytime(Duration duration) {
        long seconds = Math.max(0, duration.getSeconds());
        long years = seconds / 31_536_000;
        seconds %= 31_536_000;
        long days = seconds / 86_400;
        seconds %= 86_400;
        long hours = seconds / 3_600;
        seconds %= 3_600;
        long minutes = seconds / 60;
        seconds %= 60;

        StringBuilder builder = new StringBuilder();
        appendDurationPart(builder, years, "Jahr", "Jahre");
        appendDurationPart(builder, days, "Tag", "Tage");
        appendDurationPart(builder, hours, "Stunde", "Stunden");
        appendDurationPart(builder, minutes, "Minute", "Minuten");
        appendDurationPart(builder, seconds, "Sekunde", "Sekunden");
        return builder.isEmpty() ? "0 Sekunden" : builder.toString();
    }

    private void appendDurationPart(StringBuilder builder, long value, String singular, String plural) {
        if (value <= 0) {
            return;
        }
        if (!builder.isEmpty()) {
            builder.append(' ');
        }
        builder.append(value).append(' ').append(value == 1 ? singular : plural);
    }
}
