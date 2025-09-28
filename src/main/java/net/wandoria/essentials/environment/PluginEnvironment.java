package net.wandoria.essentials.environment;

import net.wandoria.essentials.environment.name.ServerNameProvider;
import net.wandoria.essentials.user.EssentialsOfflineUser;
import net.wandoria.essentials.user.EssentialsUser;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Essentials might be hosted in different environments. (locally / cloudnet / pterodactyl)
 */
public interface PluginEnvironment extends ServerNameProvider {

    String getServerName();

    CompletableFuture<Boolean> isPlayerOnline(UUID uuid);

    CompletableFuture<Optional<EssentialsUser>> getUser(UUID uuid);

    CompletableFuture<Optional<EssentialsUser>> getUserByName(String userName);

    CompletableFuture<List<EssentialsUser>> getUsers();


    void connectPlayerToServer(UUID player, String serverName);

    void connectPlayerToGroup(UUID player, String groupName);

    CompletableFuture<Optional<EssentialsOfflineUser>> getOfflineUser(UUID uuid);

    CompletableFuture<Optional<EssentialsOfflineUser>> getOfflineUserByName(@NonNull String playerName);
}
