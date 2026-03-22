package de.kalypzo.essentials.user.home;

import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

public interface HomeConfiguration {

    ConfigurationSection guiItemInfo();

    ConfigurationSection guiItemHome();

    ConfigurationSection guiItemHomeDeleteConfirm();

    ConfigurationSection guiItemHomeUndefined();

    ConfigurationSection guiItemHomeLocked();

    int maxHomeCount();

    List<String> guiLayout();

    String guiTitle();
}
