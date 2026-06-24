package de.kalypzo.essentials.command.chat;

import de.kalypzo.essentials.EssentialsPlugin;
import de.kalypzo.essentials.chat.emoji.EmojiGui;
import de.kalypzo.essentials.chat.emoji.EmojiRegistry;
import org.bukkit.entity.Player;

public class EmojiCommand {

    public void openEmojiGui(Player player) {
        EmojiRegistry registry = EssentialsPlugin.instance().getEmojiRegistry();
        new EmojiGui(player, registry).open();
    }
}