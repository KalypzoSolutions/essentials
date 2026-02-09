package de.kalypzo.essentials.environment;

import de.kalypzo.essentials.EssentialsPlugin;
import de.kalypzo.essentials.user.EssentialsOfflineUser;
import de.kalypzo.essentials.user.EssentialsUser;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Abstraction layer - Essentials might be hosted in different environments. (locally / cloudnet / pterodactyl)
 * Managed by {@link EssentialsPlugin}
 */
public interface PluginEnvironment {

    static PluginEnvironment getInstance() {
        return EssentialsPlugin.instance().getEnvironment();
    }

    /**
     * Get the name of the server that velocity uses.
     *
     * @return string
     */
    String getServerName();

    /**
     * Check whether a player is online on the network.
     *
     * @param uuid any player uuid
     * @return true, if the player is online on the network.
     */
    CompletableFuture<Boolean> isPlayerOnline(UUID uuid);

    /**
     * Get the user object.
     *
     * @param uuid any player uuid
     * @return an instance of EssentialsUser or empty if the player is not online.
     */
    CompletableFuture<Optional<EssentialsUser>> getUser(UUID uuid);

    /**
     * Adapts a local Bukkit {@link Player} instance to an {@link EssentialsUser}.
     *
     * @param player the Bukkit player to adapt
     * @return the corresponding {@link EssentialsUser} instance for the given player
     */
    EssentialsUser adaptLocalPlayer(Player player);

    /**
     * Get the user object.
     *
     * @param userName name of the player
     * @return an instance of EssentialsUser or empty if the player is not online.
     */
    CompletableFuture<Optional<EssentialsUser>> getUserByName(String userName);

    /**
     * Get a list of all online users.
     *
     * @return a list of all online users.
     */
    CompletableFuture<List<EssentialsUser>> getUsers();


    CompletableFuture<Boolean> connectPlayerToServer(UUID player, String serverName);

    CompletableFuture<Optional<EssentialsOfflineUser>> getOfflineUser(UUID uuid);

    CompletableFuture<Optional<EssentialsOfflineUser>> getOfflineUserByName(@NonNull String playerName);

    /**
     * Returns a list of player names that match the input.
     * <p>
     * The list is limited to 10 entries by default.
     *
     * @param input    the input string to match against player names
     * @param querying the UUID of the player who is querying for suggestions, used to exclude their own name from the suggestions
     * @return a CompletableFuture that will complete with a list of matching player names
     */
    default CompletableFuture<List<String>> suggestOfflinePlayerNames(String input, UUID querying) {
        return suggestOfflinePlayerNames(input, querying, 10);
    }


    CompletableFuture<List<String>> suggestOfflinePlayerNames(String input, UUID querying, int limit);
}
