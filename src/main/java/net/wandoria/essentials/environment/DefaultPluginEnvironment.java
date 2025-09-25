package net.wandoria.essentials.environment;

import net.wandoria.essentials.EssentialsPlugin;
import net.wandoria.essentials.environment.name.ServerNameProvider;
import net.wandoria.essentials.user.EssentialsUser;
import net.wandoria.essentials.user.NetworkEssentialsUser;
import it.einjojo.playerapi.PlayerApi;
import it.einjojo.playerapi.PlayerApiProvider;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Expects the Player-API to be available.
 */
public class DefaultPluginEnvironment implements PluginEnvironment {
    @Getter
    private final PlayerApi playerApi;
    private final EssentialsPlugin plugin;
    private final ServerNameProvider serverNameProvider;

    public DefaultPluginEnvironment(EssentialsPlugin plugin, ServerNameProvider serverNameProvider) {
        this.playerApi = PlayerApiProvider.getInstance();
        this.plugin = plugin;
        this.serverNameProvider = serverNameProvider;
    }

    @Override
    public String getServerName() {
        return serverNameProvider.getServerName();
    }

    @Override
    public CompletableFuture<Boolean> isPlayerOnline(UUID uuid) {
        return playerApi.isPlayerOnline(uuid);
    }

    @Override
    public CompletableFuture<Optional<EssentialsUser>> getUser(UUID uuid) {
        return playerApi.getOnlinePlayer(uuid).thenApply((networkPlayer -> {
            if (networkPlayer == null) return Optional.empty();
            return Optional.of(new NetworkEssentialsUser(networkPlayer));
        }));
    }

    @Override
    public CompletableFuture<Optional<EssentialsUser>> getUserByName(String userName) {
        return playerApi.getOnlinePlayer(userName).thenApply((networkPlayer -> {
            if (networkPlayer == null) return Optional.empty();
            return Optional.of(new NetworkEssentialsUser(networkPlayer));
        }));
    }

    @Override
    public CompletableFuture<List<EssentialsUser>> getUsers() {
        return playerApi.getOnlinePlayers().thenApply(players -> {
            List<EssentialsUser> users = new ArrayList<>();
            for (var player : players) {
                users.add(new NetworkEssentialsUser(player));
            }
            return users;
        });
    }

    @Override
    public void connectPlayerToServer(UUID player, String serverName) {
        playerApi.connectPlayerToServer(player, serverName);
    }

    @Override
    public void connectPlayerToGroup(UUID player, String groupName) {
        playerApi.connectPlayerToServer(player, groupName);
    }

}
