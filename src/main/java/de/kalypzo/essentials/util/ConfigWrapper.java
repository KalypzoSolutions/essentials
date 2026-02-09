package de.kalypzo.essentials.util;

import de.kalypzo.essentials.chat.ChatConfiguration;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * implements all config interfaces by reading from the "default"-config
 */
public record ConfigWrapper(JavaPlugin plugin) implements ChatConfiguration {
    private FileConfiguration config() {
        return plugin.getConfig();
    }

    @Override
    public String getChatFormat() {
        return config().getString("chat.format");
    }

    @Override
    public String getPrivateMessageFormatForSender() {
        return config().getString("chat.private.sender");
    }

    @Override
    public String getPrivateMessageFormatForReceiver() {
        return config().getString("chat.private.receiver");
    }

    @Override
    public Sound getPingSound() {
        return Sound.BLOCK_AMETHYST_BLOCK_RESONATE;
    }
}
