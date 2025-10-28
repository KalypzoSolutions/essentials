package net.wandoria.essentials.command.world;


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.wandoria.essentials.world.NetworkPosition;
import net.wandoria.essentials.world.warps.Warp;
import net.wandoria.essentials.world.warps.WarpManager;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Flag;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.paper.util.sender.PlayerSource;
import org.incendo.cloud.paper.util.sender.Source;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * <p>Provides all commands related to warps</p>
 * <p>Because of @CommandContainer it gets instantiated by {@link net.wandoria.essentials.command.CommandManager}</p>
 */
@CommandContainer
public class WarpCommand {
    @Command("warps")
    @Command("warps list")
    public void list(Source source) {
        for (Warp warp : WarpManager.getInstance().getWarps()) {
            source.source().sendMessage(warp);
        }
    }

    @Command("warp <warp>")
    public void warp(PlayerSource player, Warp warp) {
        warp.teleport(player.source());
    }

    @Command("warp reload")
    @Permission("wandoria.essentials.command.warp.reload")
    public CompletableFuture<Void> reload(Source source) {
        return WarpManager.getInstance().load().thenAccept(_void -> {
            source.source().sendMessage(Component.translatable("essentials.command.warp.reload"));
        });
    }

    @Command("warp set <name>")
    @Permission("wandoria.essentials.command.warp.reload")
    public CompletableFuture<Void> save(PlayerSource source, String name, @Nullable @Flag("permission") String permission, @Nullable @Flag("displayName") String displayName) {
        Component component;
        if (displayName != null) {
            component = MiniMessage.miniMessage().deserialize(displayName).colorIfAbsent(NamedTextColor.AQUA);
        } else {
            component = Component.text(name).colorIfAbsent(NamedTextColor.AQUA);
        }
        var warp = new Warp(name, component, permission, NetworkPosition.createByLocation(source.source().getLocation()));
        return WarpManager.getInstance().saveWarp(warp).thenAccept((_void) -> {
            source.source().sendMessage(Component.translatable("essentials.command.warp.set", warp));
        });

    }
}
