package it.einjojo.essentials.environment;

import it.einjojo.essentials.EssentialsPlugin;
import it.einjojo.essentials.user.EssentialsUser;
import it.einjojo.essentials.user.NetworkEssentialsUser;
import it.einjojo.playerapi.PlayerApi;
import it.einjojo.playerapi.PlayerApiProvider;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DefaultPluginEnvironment implements PluginEnvironment {
    @Getter
    private final PlayerApi playerApi;
    private final EssentialsPlugin plugin;

    public DefaultPluginEnvironment() {
        this.playerApi = PlayerApiProvider.getInstance();
        this.plugin = EssentialsPlugin.instance();
    }

    @Override
    public String getServerName() {
        return "";
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
