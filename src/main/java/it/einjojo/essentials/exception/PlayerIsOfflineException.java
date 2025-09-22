package it.einjojo.essentials.exception;

import lombok.Getter;

import java.util.UUID;

/**
 * Wird geworfen, wenn eine Aktion ausgeführt wird, die nur online möglich ist.
 */
@Getter
public class PlayerIsOfflineException extends IllegalStateException {
    private final UUID player;

    public PlayerIsOfflineException(UUID player) {
        super("Player with UUID " + player + " is offline but is required to be online.");
        this.player = player;
    }
}
