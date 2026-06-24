package de.kalypzo.essentials.chat.emoji;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public record Emoji(String shortcut, String character, String permission, ItemStack displayItem) {
    public boolean hasPermission(Player player) {
        return permission.isEmpty() || player.hasPermission(permission);
    }
}