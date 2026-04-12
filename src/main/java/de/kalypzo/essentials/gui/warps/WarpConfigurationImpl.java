package de.kalypzo.essentials.gui.warps;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WarpConfigurationImpl implements WarpConfiguration {

    private static final String FILE_NAME = "warps.yml";
    private FileConfiguration config;

    private WarpConfigurationImpl(FileConfiguration config) {
        this.config = config;
    }

    public static WarpConfigurationImpl load(JavaPlugin plugin) {
        File file = new File(plugin.getDataFolder(), FILE_NAME);
        if (!file.exists()) {
            plugin.saveResource(FILE_NAME, false);
        }
        return new WarpConfigurationImpl(YamlConfiguration.loadConfiguration(file));
    }

    public void reload(JavaPlugin plugin) {
        File file = new File(plugin.getDataFolder(), FILE_NAME);
        config = YamlConfiguration.loadConfiguration(file);
    }

    @Override
    public List<ConfigurationSection> guiItems() {
        ConfigurationSection itemsSection = config.getConfigurationSection("gui.items");
        if (itemsSection == null) return List.of();
        List<ConfigurationSection> result = new ArrayList<>();
        for (String key : itemsSection.getKeys(false)) {
            ConfigurationSection section = itemsSection.getConfigurationSection(key);
            if (section != null) {
                result.add(section);
            }
        }
        return result;
    }

    @Override
    public List<String> guiLayout() {
        return config.getStringList("gui.layout");
    }

    @Override
    public String guiTitle() {
        return config.getString("gui.title", "<h2>Warps");
    }
}
