package net.wandoria.essentials.util;

import net.wandoria.essentials.chat.ChatConfiguration;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * implements all config interfaces by reading from the "default"-config
 */
public record ConfigWrapper(FileConfiguration config) implements ChatConfiguration {


    @Override
    public String chatFormat() {
        return config.getString("chat.format");
    }

    @Override
    public String getPrivateMessageFormatForSender() {
        return config.getString("chat.private.sender");
    }

    @Override
    public String getPrivateMessageFormatForReceiver() {
        return config.getString("chat.private.receiver");
    }
}
