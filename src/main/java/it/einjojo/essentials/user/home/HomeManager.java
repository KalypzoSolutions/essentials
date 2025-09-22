package it.einjojo.essentials.user.home;

import it.einjojo.central.CentralPlugin;
import it.einjojo.central.positon.NetworkPosition;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class HomeManager {
    private static final int MAX_HOME_COUNT = 20;
    private final DataSource dataSource;
    private final Logger log = CentralPlugin.instance().getSLF4JLogger();

    private HomeManager(@NonNull DataSource dataSource) {
        this.dataSource = dataSource;
        prepareTables();
    }

    private static class OnDemand {
        private final static HomeManager INSTANCE = new HomeManager(CentralPlugin.instance().getHikariDataSource());
    }

    private void prepareTables() {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS public.central_home (" +
                    "owner UUID NOT NULL, " +
                    "home_name VARCHAR(255) NOT NULL, " +
                    "location VARCHAR(255) NOT NULL, " +
                    "PRIMARY KEY (owner, home_name)" +
                    ");"
            );
        } catch (SQLException e) {
            log.error("Could not prepare tables", e);
            throw new RuntimeException(e);
        }
    }


    public static HomeManager getInstance() {
        return OnDemand.INSTANCE;
    }


    public CompletableFuture<Optional<Home>> getHome(@NonNull UUID owner, @NonNull String name) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT location FROM public.central_home WHERE home_name = ? AND owner = ?")) {
                statement.setString(1, name);
                statement.setObject(2, owner);
                try (var resultSet = statement.executeQuery();) {
                    if (resultSet.next()) {
                        String location = resultSet.getString("location");
                        return Optional.of(new Home(owner, name, NetworkPosition.deserialize(location)));
                    }
                }
                return Optional.empty();
            } catch (SQLException e) {
                log.error("Could not get home {} {}", owner, name, e);
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<List<Home>> getHomes(@NonNull UUID playerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement("SELECT home_name, location FROM public.central_home WHERE owner = ?")) {
                statement.setObject(1, playerUuid);
                var resultSet = statement.executeQuery();

                List<Home> homes = new LinkedList<>();
                while (resultSet.next()) {
                    String homeName = resultSet.getString("home_name");
                    String location = resultSet.getString("location");
                    homes.add(new Home(playerUuid, homeName, NetworkPosition.deserialize(location)));
                }
                resultSet.close();
                return homes;
            } catch (SQLException e) {
                log.error("Could not get homes {}", playerUuid, e);
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Void> saveHome(Home home) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "INSERT INTO public.central_home (owner, home_name, location) VALUES (?, ?, ?) " +
                                 "ON CONFLICT (owner, home_name) DO UPDATE SET location = EXCLUDED.location")) {

                statement.setObject(1, home.owner());
                statement.setString(2, home.name());
                statement.setString(3, home.location().serialize());

                statement.executeUpdate();
            } catch (SQLException e) {
                log.error("Could not save {}", home, e);
                throw new RuntimeException(e);
            }
        });
    }

    public int getMaxHomes(Player player) {
        var meta = player.getMetadata("max-homes");
        if (meta.isEmpty()) {
            int maxHomes = 0;
            if (player.hasPermission("homes.max.*")) return MAX_HOME_COUNT;
            for (int i = 0; i < MAX_HOME_COUNT; i++) {
                if (player.hasPermission("homes.max." + i)) {
                    maxHomes = i;
                    meta.add(new FixedMetadataValue(CentralPlugin.instance(), maxHomes));
                    break;
                }
                ;
            }
            return maxHomes;
        }
        return meta.getFirst().asInt();
    }

}
