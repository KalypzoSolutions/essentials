package it.einjojo.essentials.world.warps;

import it.einjojo.central.CentralPlugin;
import it.einjojo.central.positon.NetworkPosition;
import lombok.NonNull;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class WarpManager {
    private final Logger log = CentralPlugin.instance().getSLF4JLogger();
    private final DataSource dataSource;
    private final Map<String, Warp> warps = new ConcurrentHashMap<>();

    private CompletableFuture<Void> loadFuture;

    private WarpManager(@NonNull DataSource dataSource) {
        this.dataSource = dataSource;
        prepareTables();
    }

    private static class OnDemand {
        private final static WarpManager INSTANCE = new WarpManager(CentralPlugin.instance().getHikariDataSource());
    }

    private void prepareTables() {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS public.central_warps (warp_name VARCHAR(64) PRIMARY KEY NOT NULL, location VARCHAR(255) NOT NULL);");
        } catch (SQLException e) {
            log.error("Could not prepare tables", e);
            throw new RuntimeException(e);
        }
    }


    public static WarpManager getInstance() {
        return OnDemand.INSTANCE;
    }

    public CompletableFuture<Void> load() {
        if (loadFuture != null && !loadFuture.isDone()) {
            return loadFuture;
        }
        loadFuture = CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
                try (ResultSet resultSet = statement.executeQuery("SELECT warp_name, location FROM public.central_warps;")) {
                    while (resultSet.next()) {
                        String warpName = resultSet.getString("warp_name");
                        String location = resultSet.getString("location");
                        registerWarp(new Warp(warpName, NetworkPosition.deserialize(location)));
                    }
                }
            } catch (SQLException e) {
                log.error("Could not load homes", e);
                throw new RuntimeException(e);
            }
        });
        return loadFuture;
    }


    private void registerWarp(Warp warp) {
        warps.put(warp.name(), warp);
    }

    public Collection<String> getWarpNames() {
        return warps.keySet();
    }

    public Optional<Warp> getWarp(String name) {
        return Optional.ofNullable(warps.get(name));
    }


    public CompletableFuture<Void> saveWarp(Warp warp) {
        registerWarp(warp);
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO public.central_warps (warp_name, location) VALUES (?, ?) ON CONFLICT (warp_name) DO UPDATE SET location = EXCLUDED.location")) {
                statement.setString(1, warp.name());
                statement.setString(2, warp.location().serialize());
                statement.executeUpdate();
            } catch (SQLException e) {
                log.error("Could not save {}", warp, e);
                throw new RuntimeException(e);
            }
        });
    }


}
