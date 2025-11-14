package net.wandoria.essentials.environment;

import net.wandoria.essentials.EssentialsPlugin;
import net.wandoria.essentials.environment.name.ServerNameProvider;
import net.wandoria.essentials.user.EssentialsOfflineUser;
import net.wandoria.essentials.user.EssentialsUser;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Abstraction layer - Essentials might be hosted in different environments. (locally / cloudnet / pterodactyl)
 * Managed by {@link net.wandoria.essentials.EssentialsPlugin}
 */
public interface PluginEnvironment extends ServerNameProvider {

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


    void connectPlayerToServer(UUID player, String serverName);

    void connectPlayerToGroup(UUID player, String groupName);

    CompletableFuture<Optional<EssentialsOfflineUser>> getOfflineUser(UUID uuid);

    CompletableFuture<Optional<EssentialsOfflineUser>> getOfflineUserByName(@NonNull String playerName);
}
