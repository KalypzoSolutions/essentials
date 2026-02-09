package de.kalypzo.essentials.user.leaderboard;

import it.einjojo.economy.db.AccountData;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import de.kalypzo.essentials.EssentialsPlugin;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Queries the top 10 players from the database.
 * Expects to have access to the economy-table.
 */
@Slf4j
public class BalanceTopPostgresAccessor {
    private final String selectTop10Query;
    private final DataSource dataSource;
    @Getter
    private Instant lastUpdate = Instant.MIN;
    @Getter
    private AccountData[] topTen = new AccountData[10];
    private CompletableFuture<Void> updateFuture;

    public BalanceTopPostgresAccessor(DataSource dataSource, String economyTableName) {
        this.selectTop10Query = """
                SELECT uuid, balance, version FROM %s ORDER BY balance DESC LIMIT 10;
                """.formatted(economyTableName);
        this.dataSource = dataSource;
    }

    public void refreshTopTenAsync() {
        if (isRefreshing()) {
            return;
        }
        updateFuture = CompletableFuture.runAsync(() -> {
            topTen = getTopTenFromDB();
            lastUpdate = Instant.now();
        }, EssentialsPlugin.getExecutorService());
    }

    public boolean isRefreshing() {
        return updateFuture != null && !updateFuture.isDone();
    }

    private AccountData[] getTopTenFromDB() {
        AccountData[] fetched = new AccountData[10];
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(selectTop10Query)) {
            int i = 0;
            while (rs.next()) {
                AccountData data = new AccountData(
                        rs.getObject("uuid", UUID.class),
                        rs.getDouble("balance"),
                        rs.getLong("version")
                );
                fetched[i] = data;
                i++;
            }
        } catch (SQLException e) {
            log.error("Could not fetch top ten", e);
        }
        return fetched;
    }


}
