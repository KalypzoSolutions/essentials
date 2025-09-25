package net.wandoria.essentials.event;


import net.wandoria.essentials.user.home.Home;
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
public class AsyncPlayerHomeTeleportEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled;
    private final Home home;

    public AsyncPlayerHomeTeleportEvent(@NotNull Player who, Home home) {
        super(who, true);
        this.home = home;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
