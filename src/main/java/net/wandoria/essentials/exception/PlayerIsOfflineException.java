package net.wandoria.essentials.exception;

import lombok.Getter;

import java.util.UUID;

/**
 * Gets thrown when an action requires a player to be online, but the player is offline.
 */
@Getter
public class PlayerIsOfflineException extends IllegalStateException {
    private final UUID player;

    public PlayerIsOfflineException(UUID player) {
        super("Player with UUID " + player + " is offline but is required to be online.");
        this.player = player;
    }
}
