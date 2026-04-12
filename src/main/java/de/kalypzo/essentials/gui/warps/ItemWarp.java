package de.kalypzo.essentials.gui.warps;

import de.kalypzo.essentials.gui.ItemBuilderConfigReader;
import de.kalypzo.essentials.world.warps.Warp;
import de.kalypzo.essentials.world.warps.WarpManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.impl.AbstractItem;

import java.util.Optional;

class ItemWarp extends AbstractItem {

    private static final ItemBuilderConfigReader CONFIG_READER = new ItemBuilderConfigReader();

    private final ConfigurationSection itemSection;
    private final WarpManager warpManager;

    ItemWarp(ConfigurationSection itemSection, WarpManager warpManager) {
        this.itemSection = itemSection;
        this.warpManager = warpManager;
    }

    @Override
    public ItemProvider getItemProvider(Player viewer) {
        return CONFIG_READER.parse(itemSection);
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
        if (!clickType.isLeftClick()) return;
        String destination = itemSection.getString("destination");
        if (destination == null || destination.isBlank()) return;
        Optional<Warp> warp = warpManager.getWarp(destination);
        if (warp.isEmpty()) return;
        player.closeInventory();
        warp.get().teleport(player, Warp.Reason.COMMAND);
    }
}
