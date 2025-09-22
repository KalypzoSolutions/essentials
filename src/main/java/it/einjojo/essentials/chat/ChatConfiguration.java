package it.einjojo.essentials.chat;

public interface ChatConfiguration {
    String DEFAULT_CHAT_FORMAT = "%luckperms_prefix% %player_name% <dark_gray>➥ <gray><message>";

    /**
     * minimessage template string for chat format.
     */
    String chatFormat();

    String getPrivateMessageFormatForSender();

    String getPrivateMessageFormatForReceiver();
}
