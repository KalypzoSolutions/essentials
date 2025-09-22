package it.einjojo.essentials;

import io.lettuce.core.RedisClient;
import it.einjojo.essentials.chat.ChatSystem;
import it.einjojo.essentials.environment.DefaultPluginEnvironment;
import it.einjojo.essentials.environment.PluginEnvironment;
import it.einjojo.essentials.world.PositionAccessor;
import it.einjojo.essentials.world.TeleportExecutor;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslationStore;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.util.UTF8ResourceBundleControl;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class EssentialsPlugin extends JavaPlugin {
    public static final Component PREFIX = MiniMessage.miniMessage().deserialize("<color:#7c86ff>Essentials</color> <color:#52525c>»</color> <color:#cad5e2>");
    public static final Component CHAT_PREFIX = MiniMessage.miniMessage().deserialize("<color:#7c86ff>ChatSystem</color> <color:#52525c>»</color> <color:#cad5e2>");
    public static final TextColor HIGHLIGHT = TextColor.color(0x00bcff);
    public static MiniMessage miniMessage;
    private static EssentialsPlugin instance;

    @Getter
    private PluginEnvironment environment;
    @Getter
    private ChatSystem chatSystem;

    private RedisClient redis;

    @Override
    public void onEnable() {
        instance = this;
        environment = createEnvironment();
        miniMessage = MiniMessage.builder().editTags(builder -> {
            builder.tag("prefix", Tag.inserting(PREFIX));
            builder.tag("chat", Tag.inserting(CHAT_PREFIX));
            builder.tag("hl", Tag.styling(HIGHLIGHT));
            builder.tag("ss", Tag.styling(TextColor.color(0x7bf1a8)));
            builder.tag("ex", Tag.styling(TextColor.color(0xff6467)));
        }).build();
        SharedConnectionConfiguration connectionConfiguration = SharedConnectionConfiguration.load();
        //REDIS STUFF
        getSLF4JLogger().info("Connecting to Redis...");
        redis = RedisClient.create(connectionConfiguration.getRedis().createUri("Essentials"));
        var pubSub = redis.connectPubSub();
        chatSystem = new ChatSystem(pubSub, this, null, environment.getServerName());
        TeleportExecutor.getInstance().init(pubSub);
        PositionAccessor.getInstance().init(pubSub);


        loadLocales();
    }

    private PluginEnvironment createEnvironment() {
        getSLF4JLogger().info("Determining server environment...");
        return new DefaultPluginEnvironment();
    }

    @Override
    public void onDisable() {
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

}
