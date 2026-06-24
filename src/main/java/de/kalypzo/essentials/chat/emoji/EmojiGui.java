package de.kalypzo.essentials.chat.emoji;

import de.kalypzo.essentials.EssentialsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class EmojiGui {

    private static final String TITLE = "§8Emoji Sammlung";
    private static final int GUI_SIZE = 54;

    private final Player player;
    private final List<Emoji> allEmojis;

    public EmojiGui(Player player, EmojiRegistry registry) {
        this.player = player;
        this.allEmojis = registry.getAll();
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(player, GUI_SIZE, TITLE);

        ItemStack pane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta paneMeta = pane.getItemMeta();
        paneMeta.setDisplayName(" ");
        pane.setItemMeta(paneMeta);
        for (int i = 0; i < GUI_SIZE; i++) {
            if (i < 9 || i >= GUI_SIZE - 9 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, pane);
            }
        }

        int[] slots = {10,11,12,13,14,15,16,
                19,20,21,22,23,24,25,
                28,29,30,31,32,33,34,
                37,38,39,40,41,42,43};
        int slotIndex = 0;
        for (Emoji emoji : allEmojis) {
            if (slotIndex >= slots.length) break;
            ItemStack item;
            if (emoji.hasPermission(player)) {
                item = emoji.displayItem().clone();
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName("§f" + emoji.character() + " §7" + emoji.shortcut());
                meta.setLore(List.of("§7Klick zum Einfügen"));
                item.setItemMeta(meta);
            } else {
                item = new ItemStack(Material.GRAY_DYE);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName("§8???");
                meta.setLore(List.of("§7Nicht freigeschaltet"));
                item.setItemMeta(meta);
            }
            inv.setItem(slots[slotIndex], item);
            slotIndex++;
        }

        player.openInventory(inv);
        EmojiGuiListener.register(player.getUniqueId(), this);
    }

    public void handleClick(int slot) {
        int[] slots = {10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34,37,38,39,40,41,42,43};
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == slot && i < allEmojis.size()) {
                Emoji emoji = allEmojis.get(i);
                if (emoji.hasPermission(player)) {
                    insertEmojiIntoChat(emoji.character());
                    player.closeInventory();
                }
                break;
            }
        }
    }

    private void insertEmojiIntoChat(String emojiChar) {
        EssentialsPlugin.instance().getChatSystem().sendEmojiChat(player, emojiChar);
    }
}