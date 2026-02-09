package de.kalypzo.essentials.user.home;


import de.kalypzo.essentials.EssentialsPlugin;
import de.kalypzo.essentials.util.PermissionsRange;
import de.kalypzo.essentials.world.NetworkPosition;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@NullMarked
public class HomeManager {
    private static final int MAX_HOME_COUNT = 20;
    private final DataSource dataSource;
    private final Logger log = EssentialsPlugin.instance().getSLF4JLogger();

    private HomeManager(DataSource dataSource) {
        this.dataSource = dataSource;
        for (int i = 0; i < MAX_HOME_COUNT; i++) {
            Bukkit.getPluginManager().addPermission(new Permission("essentials.homes.max." + i, "Allows to set up " + i + " homes"));
        }
    }

    public CompletableFuture<Void> deleteHome(Home home) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement("DELETE FROM essentials_player_homes WHERE owner = ? AND name = ?")) {
                statement.setObject(1, home.owner());
                statement.setString(2, home.name());
                statement.executeUpdate();
            } catch (SQLException e) {
                log.error("Could not delete {}", home, e);
                throw new RuntimeException(e);
            }
        }, EssentialsPlugin.getExecutorService());
    }

    private static class OnDemand {
        private final static HomeManager INSTANCE = new HomeManager(EssentialsPlugin.instance().getDataSource());
    }

    public static HomeManager getInstance() {
        return OnDemand.INSTANCE;
    }


    public CompletableFuture<Optional<Home>> getHome(UUID owner, String name) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT location FROM essentials_player_homes WHERE name = ? AND owner = ?")) {
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
        }, EssentialsPlugin.getExecutorService());
    }

    public CompletableFuture<List<Home>> getHomes(UUID playerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement("SELECT name, location FROM public.essentials_player_homes WHERE owner = ?")) {
                statement.setObject(1, playerUuid);
                var resultSet = statement.executeQuery();

                List<Home> homes = new LinkedList<>();
                while (resultSet.next()) {
                    String homeName = resultSet.getString("name");
                    String location = resultSet.getString("location");
                    homes.add(new Home(playerUuid, homeName, NetworkPosition.deserialize(location)));
                }
                resultSet.close();
                return homes;
            } catch (SQLException e) {
                log.error("Could not get homes {}", playerUuid, e);
                throw new RuntimeException(e);
            }
        }, EssentialsPlugin.getExecutorService());
    }

    public CompletableFuture<Void> saveHome(Home home) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "INSERT INTO essentials_player_homes (owner, name, location) VALUES (?, ?, ?) " +
                                 "ON CONFLICT (owner, name) DO UPDATE SET location = EXCLUDED.location")) {

                statement.setObject(1, home.owner());
                statement.setString(2, home.name());
                statement.setString(3, home.location().serialize());

                statement.executeUpdate();
            } catch (SQLException e) {
                log.error("Could not save {}", home, e);
                throw new RuntimeException(e);
            }
        }, EssentialsPlugin.getExecutorService());
    }

    public int getMaxHomes(Player player) {
        if (player.hasPermission("essentials.homes.max.*")) return Integer.MAX_VALUE;
        return PermissionsRange.max(player, "essentials.homes.max");

    }

}
