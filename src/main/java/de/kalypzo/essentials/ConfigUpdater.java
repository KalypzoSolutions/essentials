package de.kalypzo.essentials;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ConfigUpdater {

    private static final String VERSION_KEY = "_version";

    private final JavaPlugin plugin;
    private final int latestVersion;

    public ConfigUpdater(JavaPlugin plugin, int latestVersion) {
        this.plugin = plugin;
        this.latestVersion = latestVersion;
    }

    public void updateConfig() {
        InputStream resource = plugin.getResource("config.yml");
        if (resource == null) {
            plugin.getSLF4JLogger().warn("Default config.yml not found in plugin resources.");
            return;
        }

        YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
                new InputStreamReader(resource, StandardCharsets.UTF_8)
        );

        FileConfiguration config = plugin.getConfig();
        int currentVersion = config.getInt(VERSION_KEY, 0);

        config.setDefaults(defaults);
        config.options().copyDefaults(true);

        if (currentVersion < latestVersion) {
            config.set(VERSION_KEY, latestVersion);
            plugin.getSLF4JLogger().info("Config updated from version {} to {}.", currentVersion, latestVersion);
        }

        plugin.saveConfig();
    }
}
