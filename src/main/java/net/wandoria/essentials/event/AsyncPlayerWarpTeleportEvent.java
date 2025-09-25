package net.wandoria.essentials.event;

import net.wandoria.essentials.world.warps.Warp;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * When the player enters /home tp <\home> this event will be called
 */
@Getter
@Setter
public class AsyncPlayerWarpTeleportEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled;
    private final Warp warp;

    public AsyncPlayerWarpTeleportEvent(@NotNull Player who, Warp home) {
        super(who, true);
        this.warp = home;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
