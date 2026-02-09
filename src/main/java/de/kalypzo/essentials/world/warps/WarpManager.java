package de.kalypzo.essentials.world.warps;

import lombok.NonNull;
import net.kyori.adventure.text.minimessage.MiniMessage;
import de.kalypzo.essentials.EssentialsPlugin;
import de.kalypzo.essentials.world.NetworkPosition;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Get this class using {@link #getInstance()}
 */
public class WarpManager {
    private final Logger log = EssentialsPlugin.instance().getSLF4JLogger();
    private final DataSource dataSource;
    private final Map<String, Warp> warps = new ConcurrentHashMap<>(); // the key must be in lower-case

    private CompletableFuture<Void> loadFuture; //  load future, to prevent loading twice

    private WarpManager(@NonNull DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Collection<Warp> getWarps() {
        return warps.values();
    }

    private static class OnDemand {
        private final static WarpManager INSTANCE = new WarpManager(EssentialsPlugin.instance().getDataSource());
    }


    public static WarpManager getInstance() {
        return OnDemand.INSTANCE;
    }

    /**
     * Use this for reload / load
     *
     * @return loads all warps into memory
     */
    public CompletableFuture<Void> load() {
        if (loadFuture != null && !loadFuture.isDone()) {
            return loadFuture;
        }
        loadFuture = CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
                try (ResultSet resultSet = statement.executeQuery("SELECT name, location, display_name, permission FROM essentials_server_warps;")) {
                    while (resultSet.next()) {
                        String warpName = resultSet.getString("name");
                        String displayNameSerialized = resultSet.getString("display_name");
                        String location = resultSet.getString("location");
                        String permission = resultSet.getString("permission");
                        registerWarp(new Warp(
                                warpName,
                                MiniMessage.miniMessage().deserialize(displayNameSerialized),
                                permission,
                                NetworkPosition.deserialize(location))
                        );
                    }
                }
            } catch (SQLException e) {
                log.error("Could not load homes", e);
                throw new RuntimeException(e);
            }
        }, EssentialsPlugin.getExecutorService());
        return loadFuture;
    }


    private void registerWarp(Warp warp) {
        warps.put(warp.name().toLowerCase(), warp);
    }

    public Collection<String> getWarpNames() {
        return warps.keySet();
    }

    /**
     * Get a warp by name
     *
     * @param name the name of the warp
     * @return the warp by name (ignoring case), or empty if it doesn't exist
     */
    public Optional<Warp> getWarp(String name) {
        return Optional.ofNullable(warps.get(name.toLowerCase()));
    }


    /**
     * Creates or updates a warp
     *
     * @param warp the warp to save
     * @return a future that completes when the warp is saved or exceptionally if an error occurs
     */
    public CompletableFuture<Void> saveWarp(Warp warp) {
        registerWarp(warp);
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO essentials_server_warps (name, permission, display_name, location) VALUES (?, ?, ?, ?) ON CONFLICT (name) DO UPDATE SET location = EXCLUDED.location, permission = EXCLUDED.permission, display_name = EXCLUDED.display_name")) {
                statement.setString(1, warp.name());
                statement.setString(2, warp.permission());
                statement.setString(3, MiniMessage.miniMessage().serialize(warp.displayName()));
                statement.setString(4, warp.location().serialize());
                statement.executeUpdate();
            } catch (SQLException e) {
                log.error("Could not save {}", warp, e);
                throw new RuntimeException(e);
            }
        }, EssentialsPlugin.getExecutorService());
    }


}
