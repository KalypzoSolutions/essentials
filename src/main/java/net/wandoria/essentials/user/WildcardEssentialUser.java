package net.wandoria.essentials.user;

import net.kyori.adventure.text.Component;
import net.wandoria.essentials.EssentialsPlugin;
import net.wandoria.essentials.chat.ChatMessage;

/**
 * All Essential users on the network
 */
public class WildcardEssentialUser {
    public static final WildcardEssentialUser INSTANCE = new WildcardEssentialUser();

    /**
     * Sends a message to all players
     *
     * @param component component
     */
    public void sendMessage(Component component) {
        ChatMessage.create(component, null).deliver(EssentialsPlugin.instance().getChatSystem());
    }

}
