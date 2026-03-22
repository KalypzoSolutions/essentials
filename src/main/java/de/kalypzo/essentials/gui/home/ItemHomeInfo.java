package de.kalypzo.essentials.gui.home;

import de.kalypzo.essentials.gui.ItemBuilderConfigReader;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.impl.AbstractItem;

class ItemHomeInfo extends AbstractItem {

    private final GuiHomes homes;

    ItemHomeInfo(GuiHomes homes) {
        this.homes = homes;
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
        // info item – no interaction
    }

    @Override
    public ItemProvider getItemProvider(Player viewer) {
        int maxHomes = homes.homeManager().getMaxHomes(viewer);
        return new ItemBuilderConfigReader().parse(homes.homeManager().config().guiItemInfo(), Placeholder.unparsed("max-homes", String.valueOf(maxHomes)));
    }
}
