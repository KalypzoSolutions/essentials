package de.kalypzo.essentials.environment;

import de.kalypzo.essentials.EssentialsPlugin;
import de.kalypzo.essentials.user.EssentialsOfflineUser;
import de.kalypzo.essentials.user.EssentialsUser;
import de.kalypzo.essentials.user.NetworkEssentialsOfflineUser;
import de.kalypzo.essentials.user.NetworkEssentialsUser;
import de.kalypzo.essentials.util.servername.InternalServerName;
import it.einjojo.playerapi.PlayerApi;
import it.einjojo.playerapi.PlayerApiProvider;
import it.einjojo.playerapi.ServerConnectResult;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * Expects the Player-API to be available.
 *
 */
public class DefaultPluginEnvironment implements PluginEnvironment {
    @Getter
    private final PlayerApi playerApi;
    private final EssentialsPlugin plugin;

    public DefaultPluginEnvironment(EssentialsPlugin plugin) {
        this.playerApi = PlayerApiProvider.getInstance();
        this.plugin = plugin;
    }

    @Override
    public String getServerName() {
        return InternalServerName.get();
    }

    @Override
    public CompletableFuture<Boolean> isPlayerOnline(UUID uuid) {
        return playerApi.isPlayerOnline(uuid);
    }

    @Override
    public CompletableFuture<Optional<EssentialsUser>> getUser(UUID uuid) {
        return playerApi.getOnlinePlayer(uuid).thenApplyAsync((networkPlayer -> {
            if (networkPlayer == null) return Optional.empty();
            return Optional.of(new NetworkEssentialsUser(networkPlayer));
        }), EssentialsPlugin.getExecutorService());
    }

    @Override
    public EssentialsUser adaptLocalPlayer(Player player) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CompletableFuture<Optional<EssentialsUser>> getUserByName(String userName) {
        return playerApi.getOnlinePlayer(userName).thenApplyAsync((networkPlayer -> {
            if (networkPlayer == null) return Optional.empty();
            return Optional.of(new NetworkEssentialsUser(networkPlayer));
        }), EssentialsPlugin.getExecutorService());
    }

    @Override
    public CompletableFuture<List<EssentialsUser>> getUsers() {
        return playerApi.getOnlinePlayers().thenApplyAsync(players -> {
            List<EssentialsUser> users = new ArrayList<>();
            for (var player : players) {
                users.add(new NetworkEssentialsUser(player));
            }
            return users;
        }, EssentialsPlugin.getExecutorService());
    }

    @Override
    public CompletableFuture<Boolean> connectPlayerToServer(UUID player, String serverName) {
        return playerApi.connectPlayer(player, serverName)
                .thenApply(result -> {
                    if (result.equals(ServerConnectResult.SUCCESS)) {
                        return true;
                    } else {
                        EssentialsPlugin.instance().getSLF4JLogger().error("Failed to connect player {} to server {}: {}", player, serverName, result);
                        return false;
                    }
                })
                .exceptionally(ex -> {
                    EssentialsPlugin.instance().getSLF4JLogger().error("Failed to connect player {} to server {}", player, serverName, ex);
                    throw new RuntimeException(ex);
                });
    }

    @Override
    public CompletableFuture<Boolean> deletePlayerConnection(UUID player) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Object stub = playerApi.getClass().getSuperclass().getField("playerServiceStub").get(playerApi);
                ClassLoader classLoader = playerApi.getClass().getClassLoader();
                Class<?> logoutRequestClass = Class.forName("it.einjojo.protocol.player.LogoutRequest", true, classLoader);
                Object builder = logoutRequestClass.getMethod("newBuilder").invoke(null);
                Method setUniqueId = builder.getClass().getMethod("setUniqueId", String.class);
                Method build = builder.getClass().getMethod("build");
                Object request = build.invoke(setUniqueId.invoke(builder, player.toString()));
                Object future = stub.getClass().getMethod("logout", logoutRequestClass).invoke(stub, request);
                return ((Future<?>) future).get() != null;
            } catch (Exception ex) {
                EssentialsPlugin.instance().getSLF4JLogger().error("Failed to delete player connection {}", player, ex);
                return false;
            }
        }, EssentialsPlugin.getExecutorService());
    }

    @Override
    public CompletableFuture<ConnectionRefreshResult> refreshPlayerConnections() {
        return getUsers().thenCompose(users -> {
            String currentServer = getServerName();
            List<CompletableFuture<Boolean>> deletes = users.stream()
                    .filter(user -> currentServer.equals(user.getServerName()))
                    .filter(user -> Bukkit.getPlayer(user.getUuid()) == null)
                    .map(user -> deletePlayerConnection(user.getUuid()))
                    .toList();
            return CompletableFuture.allOf(deletes.toArray(CompletableFuture[]::new))
                    .handle((unused, throwable) -> {
                        int deleted = 0;
                        int failed = 0;
                        for (CompletableFuture<Boolean> delete : deletes) {
                            if (delete.isCompletedExceptionally() || !delete.join()) {
                                failed++;
                            } else {
                                deleted++;
                            }
                        }
                        return new ConnectionRefreshResult(users.size(), deleted, failed);
                    });
        });
    }


    @Override
    public CompletableFuture<Optional<EssentialsOfflineUser>> getOfflineUser(UUID uuid) {
        return playerApi.getOfflinePlayer(uuid).thenApply((player) ->
                Optional.ofNullable(player).map(NetworkEssentialsOfflineUser::new));
    }

    @Override

    public CompletableFuture<Optional<EssentialsOfflineUser>> getOfflineUserByName(@NonNull String playerName) {
        return playerApi.getOfflinePlayer(playerName).thenApply((player) ->
                Optional.ofNullable(player).map(NetworkEssentialsOfflineUser::new));
    }

    @Override
    public CompletableFuture<List<String>> suggestOfflinePlayerNames(String input, @Nullable UUID querying, int limit) {
        return playerApi.tabCompleteOfflinePlayers(input, querying, limit);
    }

    @Override
    public CompletableFuture<List<String>> suggestOnlinePlayerNames(String input, UUID querying, int limit) {
        return playerApi.tabCompleteOnlinePlayers(input, querying, limit);
    }
}
