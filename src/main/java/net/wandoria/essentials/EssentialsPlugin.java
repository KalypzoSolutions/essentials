package net.wandoria.essentials;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.lettuce.core.RedisClient;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslationStore;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.util.UTF8ResourceBundleControl;
import net.wandoria.essentials.chat.ChatSystem;
import net.wandoria.essentials.environment.DefaultPluginEnvironment;
import net.wandoria.essentials.environment.PluginEnvironment;
import net.wandoria.essentials.environment.name.CloudNetServerNameProvider;
import net.wandoria.essentials.environment.name.DefaultServerNameProvider;
import net.wandoria.essentials.environment.name.ServerNameProvider;
import net.wandoria.essentials.user.home.HomeManager;
import net.wandoria.essentials.util.ConfigWrapper;
import net.wandoria.essentials.world.PositionAccessor;
import net.wandoria.essentials.world.TeleportExecutor;
import net.wandoria.essentials.world.warps.WarpManager;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import org.flywaydb.core.Flyway;
import org.jetbrains.annotations.Nullable;

import javax.sql.DataSource;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Loads language files from resource bundle, and does plugin setup
 */
public class EssentialsPlugin extends JavaPlugin {
    public static final Component PREFIX = MiniMessage.miniMessage().deserialize("<color:#7c86ff>Essentials</color> <color:#52525c>»</color> <color:#cad5e2>");
    public static final Component CHAT_PREFIX = MiniMessage.miniMessage().deserialize("<color:#7c86ff>ChatSystem</color> <color:#52525c>»</color> <color:#cad5e2>");
    public static final TextColor HIGHLIGHT = TextColor.color(0x00bcff);
    public static MiniMessage miniMessage = MiniMessage.builder().editTags(builder -> {
        builder.tag("prefix", Tag.inserting(PREFIX));
        builder.tag("chat", Tag.inserting(CHAT_PREFIX));
        builder.tag("hl", Tag.styling(HIGHLIGHT));
        builder.tag("ss", Tag.styling(TextColor.color(0x7bf1a8)));
        builder.tag("ex", Tag.styling(TextColor.color(0xff6467)));
    }).build();

    private static EssentialsPlugin instance;

    @Getter
    private PluginEnvironment environment;
    @Getter
    private ChatSystem chatSystem;
    private RedisClient redis;
    private DataSource dataSource;

    @Override
    public void onLoad() {
        saveDefaultConfig();
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
        chatSystem = new ChatSystem(pubSub, this, new ConfigWrapper(getConfig()), environment.getServerName());
        TeleportExecutor.getInstance().init(pubSub);
        PositionAccessor.getInstance().init(pubSub);
        getSLF4JLogger().info("All pub sub components initialized. Client subscribed to: {}", pubSub.sync().pubsubChannels());
        // database stuff
        HikariConfig config = connectionConfiguration.getPostgres().createHikariConfig();
        config.setSchema("public");
        config.setPoolName("Essentials");
        dataSource = new HikariDataSource(config);
        Flyway flyway = Flyway.configure(getClassLoader())
                .locations("classpath:db/")
                .table("essentials_flyway_schema_history")
                .loggers("slf4j")
                .createSchemas(true)
                .baselineOnMigrate(true)
                .dataSource(dataSource)
                .load();
        try {

            flyway.migrate();
        } catch (Exception ex) {
            getSLF4JLogger().error("Could not migrate Flyway", ex);
        }
        loadLocales();
    }

    private @Nullable PluginEnvironment createEnvironment() {
        getSLF4JLogger().info("Determining server environment...");
        ServerNameProvider serverNameProvider;
        if (CloudNetServerNameProvider.isAvailable()) {
            getSLF4JLogger().info("CloudNet detected, using CloudNetServerNameProvider");
            serverNameProvider = new CloudNetServerNameProvider();
        } else {
            getSLF4JLogger().warn("CloudNet not detected, falling back to default server name provider");
            serverNameProvider = new DefaultServerNameProvider();
        }
        try {
            return new DefaultPluginEnvironment(this, serverNameProvider);
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
    }

    private void loadLocales() {
        MiniMessageTranslationStore translationStore = MiniMessageTranslationStore.create(new NamespacedKey(this, "messages"), miniMessage);
        for (Locale locale : List.of(Locale.GERMANY)) {
            ResourceBundle bundle = ResourceBundle.getBundle("lang.messages", locale, getClassLoader(), UTF8ResourceBundleControl.get());
            translationStore.registerAll(locale, bundle, false);
        }
        //Languages.getInstance().setLanguageProvider(player -> player.locale().toString()); InvUI Translations
        translationStore.defaultLocale(Locale.GERMANY);
        GlobalTranslator.translator().addSource(translationStore);
    }

    public static PluginEnvironment environment() {
        return instance.environment;
    }

    public static EssentialsPlugin instance() {
        return instance;
    }

    public WarpManager getWarpManager() {
        return WarpManager.getInstance();
    }

    public HomeManager getHomeManager() {
        return HomeManager.getInstance();
    }

    public PositionAccessor getPositionAccessor() {
        return PositionAccessor.getInstance();
    }

    public TeleportExecutor getTeleportExecutor() {
        return TeleportExecutor.getInstance();
    }

    public DataSource getDataSource() {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource is not initialized yet");
        }
        return dataSource;
    }
}
