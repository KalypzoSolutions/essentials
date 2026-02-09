package de.kalypzo.essentials.command.world;


import de.kalypzo.essentials.command.CommandManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import de.kalypzo.essentials.world.NetworkPosition;
import de.kalypzo.essentials.world.warps.Warp;
import de.kalypzo.essentials.world.warps.WarpManager;
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
 * <p>Because of @CommandContainer it gets instantiated by {@link CommandManager}</p>
 */
@CommandContainer
public class WarpCommand {
    public static final String SET_PERMISSION = "essentials.command.warp.set";

    @Command("warps")
    @Command("warps list")
    public void list(Source source) {
        var warps = WarpManager.getInstance().getWarps();
        if (warps.isEmpty()) {
            source.source().sendMessage(Component.translatable("essentials.warp.warps-empty"));
            return;
        }
        for (Warp warp : warps) {
            source.source().sendMessage(warp);
        }
    }

    @Command("warp <warp>")
    public void warp(PlayerSource player, Warp warp) {
        warp.teleport(player.source(), Warp.Reason.COMMAND);
    }

    @Command("warps reload")
    @Permission("essentials.command.warp.reload")
    public CompletableFuture<Void> reload(Source source) {
        return WarpManager.getInstance().load().thenAccept(_void -> {
            source.source().sendMessage(Component.translatable("essentials.warp.reload"));
        });
    }

    @Command("warps set <name>")
    @Permission(SET_PERMISSION)
    public CompletableFuture<Void> save(PlayerSource source, String name, @Nullable @Flag("permission") String permission, @Nullable @Flag("displayName") String displayName) {
        Component component;
        if (displayName != null) {
            component = MiniMessage.miniMessage().deserialize(displayName).colorIfAbsent(NamedTextColor.AQUA);
        } else {
            component = Component.text(name).colorIfAbsent(NamedTextColor.AQUA);
        }
        var warp = new Warp(name, component, permission, NetworkPosition.createByLocation(source.source().getLocation()));
        return WarpManager.getInstance().saveWarp(warp).thenAccept((_void) -> {
            source.source().sendMessage(Component.translatable("essentials.warp.set", warp));
        });

    }

    @Command("spawn|hub|l")
    public void spawn(PlayerSource source) {
        WarpManager.getInstance().getWarp("spawn").ifPresentOrElse(warp -> warp.teleport(source.source(), Warp.Reason.COMMAND),
                () -> source.source().sendMessage(Component.translatable("essentials.warp.not-found", Component.text("spawn"))));
    }

    @Command("farmwelt")
    public void hub(PlayerSource source) {
        WarpManager.getInstance().getWarp("farmwelt").ifPresentOrElse(warp -> warp.teleport(source.source(), Warp.Reason.COMMAND),
                () -> source.source().sendMessage(Component.translatable("essentials.warp.not-found", Component.text("farmwelt"))));
    }

    @Command("nether")
    public void nether(PlayerSource source) {
        WarpManager.getInstance().getWarp("nether").ifPresentOrElse(warp -> warp.teleport(source.source(), Warp.Reason.COMMAND),
                () -> source.source().sendMessage(Component.translatable("essentials.warp.not-found", Component.text("nether"))));
    }


}
