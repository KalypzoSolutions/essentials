package net.wandoria.essentials.world.warps;


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.event.ClickEvent;
import net.wandoria.essentials.EssentialsPlugin;
import net.wandoria.essentials.event.AsyncPlayerWarpTeleportEvent;
import net.wandoria.essentials.world.NetworkPosition;
import net.wandoria.essentials.world.TeleportExecutor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * Simple Warp
 *
 * @param name        id of the warp (must be alphanumeric according to {@link Warp#NAME_PATTERN}) and max 64 chars,
 * @param displayName display name, which will be shown to players
 * @param permission  the warp's permission node, length must be below 129
 * @param location    the warp's location
 */
public record Warp(@NotNull String name,
                   @NotNull Component displayName,
                   @NotNull String permission,
                   @NotNull NetworkPosition location) implements ComponentLike {
    public static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{1,64}$");

    public Warp {
        if (!NAME_PATTERN.matcher(name).matches()) {
            throw new IllegalArgumentException("Warp name must be alphanumeric and max 64 chars");
        }
        if (permission.length() >= 128) {
            throw new IllegalArgumentException("Warp permission must be exact or less than 128 chars");
        }
    }

    /**
     * Teleports the player to the warp. Calls {@link AsyncPlayerWarpTeleportEvent} which can be canceled.
     * will never be executed on the main thread. If called from the main thread, it will be scheduled asynchronously.
     *
     * @param player the player to teleport
     */
    public void teleport(Player player) {
        if (Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTaskAsynchronously(EssentialsPlugin.instance(), () -> teleport(player));
            return;
        }
        if (!new AsyncPlayerWarpTeleportEvent(player, this).callEvent()) {
            return;
        }
        player.sendMessage(Component.translatable("essentials.warp.teleporting", Component.text(name)));
        TeleportExecutor.getInstance().teleportPlayerToPosition(player.getUniqueId(), location);
    }

    @Override
    public @NotNull Component asComponent() {
        return displayName.clickEvent(ClickEvent.runCommand("/warp " + name)).hoverEvent(Component.translatable("essentials.warp.click_to_teleport", displayName));
    }
}
