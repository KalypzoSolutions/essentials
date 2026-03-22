package de.kalypzo.essentials.user.home;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;

public class HomeConfigurationImpl implements HomeConfiguration {

    private static final String FILE_NAME = "homes.yml";
    private FileConfiguration config;

    private HomeConfigurationImpl(FileConfiguration config) {
        this.config = config;
    }

    public static HomeConfigurationImpl load(JavaPlugin plugin) {
        File file = new File(plugin.getDataFolder(), FILE_NAME);
        if (!file.exists()) {
            plugin.saveResource(FILE_NAME, false);
        }
        return new HomeConfigurationImpl(YamlConfiguration.loadConfiguration(file));
    }

    public void reload(JavaPlugin plugin) {
        File file = new File(plugin.getDataFolder(), FILE_NAME);
        config = YamlConfiguration.loadConfiguration(file);
    }

    @Override
    public ConfigurationSection guiItemInfo() {
        return config.getConfigurationSection("gui.items.info");
    }

    @Override
    public ConfigurationSection guiItemHome() {
        return config.getConfigurationSection("gui.items.home");
    }

    @Override
    public ConfigurationSection guiItemHomeDeleteConfirm() {
        return config.getConfigurationSection("gui.items.home_delete_confirm");
    }

    @Override
    public ConfigurationSection guiItemHomeUndefined() {
        return config.getConfigurationSection("gui.items.home_undefined");
    }

    @Override
    public ConfigurationSection guiItemHomeLocked() {
        return config.getConfigurationSection("gui.items.home_locked");
    }

    @Override
    public int maxHomeCount() {
        return config.getInt("max-homes", 16);
    }

    @Override
    public List<String> guiLayout() {
        return config.getStringList("gui.layout");
    }

    @Override
    public String guiTitle() {
        return config.getString("gui.title", "<h2>Homes");
    }
}
