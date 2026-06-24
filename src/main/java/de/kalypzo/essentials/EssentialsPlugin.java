package de.kalypzo.essentials;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.kalypzo.essentials.broadcast.BroadcastManager;
import de.kalypzo.essentials.chat.ChatSystem;
import de.kalypzo.essentials.chat.emoji.Emoji;
import de.kalypzo.essentials.chat.emoji.EmojiGuiListener;
import de.kalypzo.essentials.chat.emoji.EmojiRegistry;
import de.kalypzo.essentials.command.ImperatCommandLoader;
import de.kalypzo.essentials.command.chat.TeamChatCommand;
import de.kalypzo.essentials.environment.DefaultPluginEnvironment;
import de.kalypzo.essentials.environment.PluginEnvironment;
import de.kalypzo.essentials.gui.warps.WarpConfigurationImpl;
import de.kalypzo.essentials.listener.DeathListener;
import de.kalypzo.essentials.listener.JoinSpawnLocationListener;
import de.kalypzo.essentials.rce.RemoteCommandExecutor;
import de.kalypzo.essentials.user.back.BackManager;
import de.kalypzo.essentials.user.home.HomeConfigurationImpl;
import de.kalypzo.essentials.user.home.HomeManager;
import de.kalypzo.essentials.util.ConfigWrapper;
import de.kalypzo.essentials.util.Text;
import de.kalypzo.essentials.world.PositionAccessor;
import de.kalypzo.essentials.world.TeleportExecutor;
import de.kalypzo.essentials.world.warps.WarpManager;
import io.lettuce.core.RedisClient;
import it.einjojo.economy.EconomyService;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.flywaydb.core.Flyway;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.InvUI;
import xyz.xenondevs.invui.item.builder.ItemBuilder;

import javax.sql.DataSource;
import java.io.File;
import java.io.ObjectInputFilter;
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
    @Getter
    private BroadcastManager broadcastManager;
    private HomeConfigurationImpl homesConfig;
    @Getter
    private WarpConfigurationImpl warpsConfig;
    @Getter
    private EmojiRegistry emojiRegistry;


    @Override
    public void onLoad() {
        saveDefaultConfig();
        new ConfigUpdater(this, LATEST_CONFIG_VERSION).updateConfig();

    }

    @Override
    public void onEnable() {
        if (!SupportedVersion.isSupported(this)) {
            getSLF4JLogger().error(
                    "Unsupported Minecraft version '{}'. Supported versions: {}. Disabling SimpleShops.",
                    getServer().getMinecraftVersion(),
                    SupportedVersion.supportedValues()
            );
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        instance = this;
        InvUI.getInstance().setPlugin(this);
        Text.loadBranding(this);
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
        broadcastManager = new BroadcastManager(this);
        broadcastManager.start();
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


        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new EmojiGuiListener(), this);

        emojiRegistry = new EmojiRegistry();
        loadEmojis();

        // Init section
        homesConfig = HomeConfigurationImpl.load(this);
        warpsConfig = WarpConfigurationImpl.load(this);
        HomeManager.init(dataSource, homesConfig);
        chatSystem = new ChatSystem(pubSub, this, new ConfigWrapper(this), environment.getServerName());
        TeamChatCommand.setTeamChatService(chatSystem.getTeamChatService());
        TeleportExecutor.getInstance().init(pubSub);
        PositionAccessor.getInstance().init(pubSub);
        RemoteCommandExecutor.getInstance().init(pubSub);
        getSLF4JLogger().info("All pub sub components initialized. Client subscribed to: {}", pubSub.sync().pubsubChannels());
        new LocaleLoader(this, getClassLoader()).loadLocales();
        new ImperatCommandLoader(this).load();
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
        if (broadcastManager != null) {
            broadcastManager.stop();
        }
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

    private void loadEmojis() {
        File emojiFile = new File(getDataFolder(), "emojis.yml");
        if (!emojiFile.exists()) {
            saveResource("emojis.yml", false);
        }
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(emojiFile);
        emojiRegistry.clear();

        if (!cfg.contains("emojis")) return;

        for (String key : cfg.getConfigurationSection("emojis").getKeys(false)) {
            String path = "emojis." + key;
            String shortcut = key;
            String character = cfg.getString(path + ".character");
            String permission = cfg.getString(path + ".permission", "");
            String matName = cfg.getString(path + ".material", "PAPER");
            Material material = Material.getMaterial(matName.toUpperCase());
            if (material == null) material = Material.PAPER;

            ItemStack displayItem = new ItemBuilder(material)
                    .setDisplayName(character) // wird im GUI überschrieben
                    .get();

            emojiRegistry.register(new Emoji(shortcut, character, permission, displayItem));
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

    public void reloadBroadcasts() {
        if (broadcastManager != null) {
            broadcastManager.reload();
        }
    }

    public void reloadHomesConfig() {
        if (homesConfig != null) {
            homesConfig.reload(this);
        }
    }

    public void reloadWarpsConfig() {
        if (warpsConfig != null) {
            warpsConfig.reload(this);
        }
    }

    public EconomyService economyService() {
        return getServer().getServicesManager().load(EconomyService.class);
    }
}
