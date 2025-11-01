package net.wandoria.essentials.chat;

import org.bukkit.Sound;

public interface ChatConfiguration {
    String DEFAULT_CHAT_FORMAT = "%luckperms_prefix% %player_name% <dark_gray>➥ <gray><message>";

    /**
     * minimessage template string for chat format.
     */
    String getChatFormat();

    String getPrivateMessageFormatForSender();

    String getPrivateMessageFormatForReceiver();

    Sound getPingSound();
}
