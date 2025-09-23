package it.einjojo.essentials.environment;

import it.einjojo.essentials.environment.name.ServerNameProvider;
import it.einjojo.essentials.user.EssentialsUser;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Beschreibt die Umgebung, in der sich das Plugin befindet.
 * <p>
 * z.B CloudNet, Test Server, mit PlayerApi Zugriff, ...
 */
public interface PluginEnvironment extends ServerNameProvider {

    String getServerName();

    CompletableFuture<Boolean> isPlayerOnline(UUID uuid);

    CompletableFuture<Optional<EssentialsUser>> getUser(UUID uuid);

    CompletableFuture<Optional<EssentialsUser>> getUserByName(String userName);

    CompletableFuture<List<EssentialsUser>> getUsers();


    void connectPlayerToServer(UUID player, String serverName);

    void connectPlayerToGroup(UUID player, String groupName);

}
