package de.kalypzo.essentials.environment;

import de.kalypzo.essentials.EssentialsPlugin;
import de.kalypzo.essentials.user.EssentialsOfflineUser;
import de.kalypzo.essentials.user.EssentialsUser;
import de.kalypzo.essentials.user.NetworkEssentialsOfflineUser;
import de.kalypzo.essentials.user.NetworkEssentialsUser;
import de.kalypzo.essentials.util.FuzzySearch;
import de.kalypzo.essentials.util.servername.InternalServerName;
import it.einjojo.playerapi.PlayerApi;
import it.einjojo.playerapi.PlayerApiProvider;
import it.einjojo.playerapi.ServerConnectResult;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

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
                .thenApply(result -> result.equals(ServerConnectResult.SUCCESS))
                .exceptionally(ex -> {
                    EssentialsPlugin.instance().getSLF4JLogger().error("Failed to connect player {} to server {}", player, serverName, ex);
                    throw new RuntimeException(ex);
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

        // Collect all offline player names, excluding the querying player
        List<String> candidateNames;
        candidateNames = Arrays.stream(Bukkit.getOfflinePlayers())
                .map(org.bukkit.OfflinePlayer::getName)
                .filter(Objects::nonNull)
                .toList();
        if (input == null || input.trim().isEmpty() || limit <= 0) {
            return CompletableFuture.completedFuture(candidateNames.stream().limit(limit).toList());
        }
        // Use fuzzy search to rank and filter matches (case-insensitive)
        List<String> suggestions = FuzzySearch.suggest(input, candidateNames, limit);

        return CompletableFuture.completedFuture(suggestions);
    }

}
