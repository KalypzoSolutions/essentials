package net.wandoria.essentials.world;

/**
 * Describes the result of a (cross server) teleportation attempt.
 *
 * @since 1.2.0
 */
public enum TeleportResult {

    /**
     * Connecting the player to the destination server failed.
     */
    SERVER_CONNECTION_FAILED,
    /**
     * The destination player is not online.
     */
    DESTINATION_PLAYER_OFFLINE,
    /**
     * The player that should be teleported is not online.
     */
    TELEPORTING_PLAYER_OFFLINE, SUCCESS,
}
