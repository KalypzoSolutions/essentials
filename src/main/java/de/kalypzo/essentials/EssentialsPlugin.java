package de.kalypzo.essentials;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.kalypzo.essentials.chat.ChatSystem;
import de.kalypzo.essentials.command.CommandManager;
import de.kalypzo.essentials.environment.DefaultPluginEnvironment;
import de.kalypzo.essentials.environment.PluginEnvironment;
import de.kalypzo.essentials.listener.DeathListener;
import de.kalypzo.essentials.listener.JoinSpawnLocationListener;
import de.kalypzo.essentials.rce.RemoteCommandExecutor;
import de.kalypzo.essentials.user.back.BackManager;
import de.kalypzo.essentials.util.ConfigWrapper;
import de.kalypzo.essentials.world.PositionAccessor;
import de.kalypzo.essentials.world.TeleportExecutor;
import de.kalypzo.essentials.world.warps.WarpManager;
import io.lettuce.core.RedisClient;
import lombok.Getter;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.flywaydb.core.Flyway;
import org.jetbrains.annotations.Nullable;

import javax.sql.DataSource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Loads language files from resource bundle, and does plugin setup
 */
public class EssentialsPlugin extends JavaPlugin {
    private static final int LATEST_CONFIG_VERSION = 2;
    @Getter
    private static ExecutorService executorService = Executors.newCachedThreadPool();
    private static EssentialsPlugin instance;

    @Getter
    private PluginEnvironment environment;
    @Getter
    private ChatSystem chatSystem;
    @Getter
    private RedisClient redis;
    private DataSource dataSource;


    @Override
    public void onLoad() {
        saveDefaultConfig();
        int configVersion = getConfig().getInt("config-version", 1);
        if (configVersion < LATEST_CONFIG_VERSION) {
            new ConfigUpdater(this).updateConfig();
        }

    }

    @Override
    public void onEnable() {
        instance = this;
        environment = createEnvironment();
        if (environment == null) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        SharedConnectionConfiguration connectionConfiguration = SharedConnectionConfiguration.load();
        //REDIS STUFF
        getSLF4JLogger().info("Connecting to Redis...");
        redis = RedisClient.create(connectionConfiguration.getRedis().createUri("Essentials"));
        var pubSub = redis.connectPubSub();
        try {
            getSLF4JLogger().info("Pinged redis: {}.", pubSub.sync().ping());
        } catch (Exception ex) {
            getSLF4JLogger().error("Ping to redis failed. Disabling plugin", ex);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        // database stuff
        HikariConfig config = connectionConfiguration.getPostgres().createHikariConfig();
        config.setSchema("public");
        config.setPoolName("Essentials");
        dataSource = new HikariDataSource(config);
        Flyway flyway = Flyway.configure(getClassLoader())
                .locations("classpath:db/")
                .baselineOnMigrate(true)
                .baselineVersion("0.0.1")
                .table("essentials_flyway_schema_history")
                .loggers("slf4j")
                .createSchemas(true)
                .dataSource(dataSource)
                .load();
        try {
            flyway.migrate();
        } catch (Exception ex) {
            getSLF4JLogger().error("Could not migrate Flyway", ex);
        }

        // Init section
        chatSystem = new ChatSystem(pubSub, this, new ConfigWrapper(this), environment.getServerName());
        TeleportExecutor.getInstance().init(pubSub);
        PositionAccessor.getInstance().init(pubSub);
        RemoteCommandExecutor.getInstance().init(pubSub);
        getSLF4JLogger().info("All pub sub components initialized. Client subscribed to: {}", pubSub.sync().pubsubChannels());
        new LocaleLoader(this, getClassLoader()).loadLocales();
        new CommandManager(this);
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new DeathListener(BackManager.getInstance()), this);
        pm.registerEvents(new JoinSpawnLocationListener(), this);
        WarpManager.getInstance().load().whenComplete(((unused, throwable) -> {
            if (throwable != null) {
                getSLF4JLogger().error("Could not load warps", throwable);
            } else {
                getSLF4JLogger().info("Warps loaded");
            }
        }));
    }

    /**
     *
     * @return
     */
    private @Nullable PluginEnvironment createEnvironment() {
        getSLF4JLogger().info("Determining server environment...");

        try {
            return new DefaultPluginEnvironment(this);
        } catch (NoClassDefFoundError noClass) {
            getSLF4JLogger().error("The Environment could not be created because a class could not be found. Probably the player api has failed to load!");
        } catch (Exception ex) {
            getSLF4JLogger().error("The Environment could not be created.", ex);
        }
        return null;
    }

    @Override
    public void onDisable() {
        if (dataSource != null) {
            getSLF4JLogger().info("Closing database connection...");
            ((HikariDataSource) dataSource).close();
            getSLF4JLogger().info("Database connection closed.");
        }
        if (redis != null) {
            getSLF4JLogger().info("Closing Redis connection...");
            redis.shutdown();
            getSLF4JLogger().info("Redis connection closed.");
        }
        executorService.shutdown();
        boolean terminated;
        try {
            terminated = executorService.awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            getSLF4JLogger().error("Interrupted while waiting for executor service to shutdown", e);
            terminated = false;
        }
        if (!terminated) {
            getSLF4JLogger().warn("Executor service did not terminate in time, forcefully shutting down.");
            executorService.shutdownNow();
        }
    }


    public static PluginEnvironment environment() {
        return instance.environment;
    }

    public static EssentialsPlugin instance() {
        return instance;
    }

    public DataSource getDataSource() {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource is not initialized yet");
        }
        return dataSource;
    }
}
