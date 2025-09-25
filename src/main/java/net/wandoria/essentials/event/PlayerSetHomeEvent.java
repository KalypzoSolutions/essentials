package net.wandoria.essentials.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Prevent users from setting homes
 */
@Setter
@Getter
public class PlayerSetHomeEvent extends PlayerEvent implements Cancellable {
    private boolean cancelled;
    private static final HandlerList HANDLERS = new HandlerList();

    public PlayerSetHomeEvent(@NotNull Player who) {
        super(who, false);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
