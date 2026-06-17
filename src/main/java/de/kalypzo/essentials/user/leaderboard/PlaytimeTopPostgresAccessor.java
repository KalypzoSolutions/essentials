package de.kalypzo.essentials.user.leaderboard;

import de.kalypzo.essentials.EssentialsPlugin;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Queries the top 10 playtimes from the player-service database.
 */
@Slf4j
public class PlaytimeTopPostgresAccessor {
    private static final String SELECT_TOP_10_QUERY = """
            SELECT player_id AS uuid, name, playtime_millis AS playtime
            FROM player_srv_offline_players
            ORDER BY playtime_millis DESC
            LIMIT 10;
            """;
    private static final String SELECT_TOTAL_QUERY = """
            SELECT COALESCE(SUM(playtime_millis), 0) AS total
            FROM player_srv_offline_players;
            """;
    private final DataSource dataSource;
    @Getter
    private Instant lastUpdate = Instant.MIN;
    @Getter
    private PlaytimeTopEntry[] topTen = new PlaytimeTopEntry[10];
    @Getter
    private Duration totalPlaytime = Duration.ZERO;
    @Getter
    private CompletableFuture<Void> updateFuture;

    public PlaytimeTopPostgresAccessor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void refreshTopTenAsync() {
        if (isRefreshing()) {
            return;
        }
        updateFuture = CompletableFuture.runAsync(() -> {
            topTen = getTopTenFromDB();
            totalPlaytime = getTotalPlaytimeFromDB();
            lastUpdate = Instant.now();
        }, EssentialsPlugin.getExecutorService());
    }

    public boolean isRefreshing() {
        return updateFuture != null && !updateFuture.isDone();
    }

    private PlaytimeTopEntry[] getTopTenFromDB() {
        PlaytimeTopEntry[] fetched = new PlaytimeTopEntry[10];
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(SELECT_TOP_10_QUERY)) {
            int i = 0;
            while (rs.next() && i < fetched.length) {
                fetched[i] = new PlaytimeTopEntry(
                        readUuid(rs),
                        rs.getString("name"),
                        Duration.ofMillis(Math.max(0, rs.getLong("playtime")))
                );
                i++;
            }
        } catch (SQLException e) {
            log.error("Could not fetch top playtimes", e);
        }
        return fetched;
    }

    private Duration getTotalPlaytimeFromDB() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(SELECT_TOTAL_QUERY)) {
            if (rs.next()) {
                return Duration.ofMillis(Math.max(0, rs.getLong("total")));
            }
        } catch (SQLException e) {
            log.error("Could not fetch total playtime", e);
        }
        return Duration.ZERO;
    }

    private UUID readUuid(ResultSet rs) throws SQLException {
        Object uuid = rs.getObject("uuid");
        if (uuid instanceof UUID parsed) {
            return parsed;
        }
        return UUID.fromString(String.valueOf(uuid));
    }

    public record PlaytimeTopEntry(UUID uuid, String name, Duration playtime) {
    }
}
