package de.kalypzo.essentials.chat;

import org.bukkit.Sound;

public interface ChatConfiguration {

    String DEFAULT_CHAT_FORMAT =
            "%luckperms_prefix% %player_name% <dark_gray>➥ <gray><message>";

    String TEAM_CHAT_FORMAT =
            "<#a855f7>[<b>TC</b>]</#a855f7> " +
                    "<white>%luckperms_prefix% %player_name%</white> " +
                    "<gray>»</gray> " +
                    "<white><message></white>";

    String getChatFormat();

    String getPrivateMessageFormatForSender();

    String getPrivateMessageFormatForReceiver();

    Sound getPingSound();

    default String getTeamChatFormat() {
        return TEAM_CHAT_FORMAT;
    }
}