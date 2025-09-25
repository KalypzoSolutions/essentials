package net.wandoria.essentials;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zaxxer.hikari.HikariConfig;
import io.lettuce.core.RedisURI;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

@Getter
public class SharedConnectionConfiguration {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();
    private RedisConnectionConfiguration redis = new RedisConnectionConfiguration("localhost", 6379, "default", "default", false);
    private PostgresConfiguration postgres = new PostgresConfiguration(
            "jdbc:postgresql://localhost:5432/minecraft",
            "root",
            "password",
            PostgresConfiguration.DEFAULT_POOL_SETTINGS
    );


    public static SharedConnectionConfiguration load() {
        Path configFile = Paths.get("connections.json");
        try {
            if (!Files.exists(configFile)) {
                SharedConnectionConfiguration defaultConfig = new SharedConnectionConfiguration();
                String json = GSON.toJson(defaultConfig);
                Files.write(configFile, json.getBytes());
                return defaultConfig;
            }
            String json = new String(Files.readAllBytes(configFile));
            return GSON.fromJson(json, SharedConnectionConfiguration.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load connections.json", e);
        }
    }

    public record RedisConnectionConfiguration(String host, int port, String username, String password,
                                               boolean ssl) {
        public RedisURI createUri(String clientName) {
            return RedisURI.builder()
                    .withHost(host)
                    .withPort(port)
                    .withAuthentication(username, password)
                    .withClientName(clientName)
                    .withSsl(ssl)
                    .build();
        }
    }

    public record PostgresConfiguration(String jdbcUrl, String username, String password,
                                        Map<String, Object> poolSettings) {
        public static Map<String, Object> DEFAULT_POOL_SETTINGS = Map.of(
                "maximum-pool-size", "16",
                "max-idle", "8",
                "minimum-idle", "0"
        );

        /**
         * Converts driver properties to a java.util.Properties object, suitable for HikariConfig.setDataSourceProperties.
         * Only includes String values.
         *
         * @return Property object containing string-based driver properties.
         */
        @NotNull
        public Properties dataSourceProperties() {
            Properties props = new Properties();

            props.setProperty("cachePrepStmts", "true");
            props.setProperty("prepStmtCacheSize", "250");
            props.setProperty("prepStmtCacheSqlLimit", "2048");
            props.setProperty("useServerPrepStmts", "true");
            props.setProperty("useLocalSessionState", "true");
            props.setProperty("rewriteBatchedStatements", "true");
            props.setProperty("cacheResultSetMetadata", "true");
            props.setProperty("cacheServerConfiguration", "true");
            props.setProperty("elideSetAutoCommits", "true");
            props.setProperty("maintainTimeStats", "false");
            poolSettings.forEach((key, value) -> {
                if (value instanceof String) { // HikariCP often expects String properties here
                    props.setProperty(key, (String) value);
                } else if (value != null) {
                    props.setProperty(key, value.toString()); // Convert other types to string
                }
            });
            return props;
        }

        public HikariConfig createHikariConfig() {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(username);
            config.setPassword(password);
            config.setDriverClassName("org.postgresql.Driver");
            config.setDataSourceProperties(dataSourceProperties());
            return config;
        }


    }

}