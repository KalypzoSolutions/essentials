package de.kalypzo.essentials.gui.warps;

import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

public interface WarpConfiguration {

    List<ConfigurationSection> guiItems();

    List<String> guiLayout();

    String guiTitle();
}
