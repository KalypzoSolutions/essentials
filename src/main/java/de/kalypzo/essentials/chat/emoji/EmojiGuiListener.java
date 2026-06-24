package de.kalypzo.essentials.chat.emoji;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EmojiGuiListener implements Listener {

    private static final Map<UUID, EmojiGui> openGuis = new HashMap<>();

    public static void register(UUID uuid, EmojiGui gui) {
        openGuis.put(uuid, gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        UUID uuid = player.getUniqueId();
        EmojiGui gui = openGuis.get(uuid);
        if (gui == null) return;

        Inventory inv = event.getInventory();
        if (inv == null || !inv.equals(event.getView().getTopInventory())) return;
        if (event.getCurrentItem() == null) return;

        event.setCancelled(true);

        int slot = event.getSlot();
        gui.handleClick(slot);
    }

    @EventHandler
    public void onInventoryClose(org.bukkit.event.inventory.InventoryCloseEvent event) {
        openGuis.remove(event.getPlayer().getUniqueId());
    }
}