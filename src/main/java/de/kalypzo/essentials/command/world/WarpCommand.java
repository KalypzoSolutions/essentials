package de.kalypzo.essentials.command.world;


import de.kalypzo.essentials.EssentialsPlugin;
import de.kalypzo.essentials.command.CommandManager;
import de.kalypzo.essentials.gui.warps.GuiWarps;
import de.kalypzo.essentials.world.NetworkPosition;
import de.kalypzo.essentials.world.warps.Warp;
import de.kalypzo.essentials.world.warps.WarpManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
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
 * <p>Because of @CommandContainer it gets instantiated by {@link CommandLoader}</p>
 */
@CommandContainer
public class WarpCommand {
    public static final String SET_PERMISSION = "essentials.command.warp.set";
    public static final String DELETE_PERMISSION = "essentials.command.warp.delete";

    @Command("warps")
    @Command("warps list")
    public void list(PlayerSource source) {
        try {
            new GuiWarps(source.source(), WarpManager.getInstance(), EssentialsPlugin.instance().getWarpsConfig()).open();
        } catch (Exception e) {
            EssentialsPlugin.instance().getSLF4JLogger().error("Failed to open warps GUI for player {}: {}", source.source().getName(), e.getMessage());
            source.source().sendMessage(Component.translatable("essentials.warp.gui-load-failed"));
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

    @Command("warps delete <warp>")
    @Permission(DELETE_PERMISSION)
    public CompletableFuture<Void> delete(PlayerSource source, Warp warp) {
        return WarpManager.getInstance().deleteWarp(warp).thenAccept(_v -> {
            source.source().sendMessage(Component.translatable("essentials.warp.deleted", warp));

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
