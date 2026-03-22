package de.kalypzo.essentials.gui.home;

import de.kalypzo.essentials.EssentialsPlugin;
import de.kalypzo.essentials.gui.ItemBuilderConfigReader;
import de.kalypzo.essentials.user.home.Home;
import de.kalypzo.essentials.user.home.HomeConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.impl.AbstractItem;

class ItemHome extends AbstractItem {
    private static final ItemBuilderConfigReader CONFIG_READER = new ItemBuilderConfigReader();
    private final GuiHomes gui;
    private @Nullable Home home;
    private boolean locked;
    private boolean deleteConfirm;

    public ItemHome(GuiHomes gui) {
        this.gui = gui;
    }

    @Override
    public ItemProvider getItemProvider(Player viewer) {
        HomeConfiguration config = gui.homeManager().config();

        if (locked) {
            return CONFIG_READER.parse(config.guiItemHomeLocked());
        }

        if (home == null) {
            return CONFIG_READER.parse(config.guiItemHomeUndefined());
        }

        if (deleteConfirm) {
            return CONFIG_READER.parse(config.guiItemHomeDeleteConfirm(), home);
        }

        return CONFIG_READER.parse(config.guiItemHome(), home);
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
        if (locked || home == null) return;

        if (clickType.isRightClick()) {
            if (deleteConfirm) {
                deleteConfirm = false;
                delete(player);
            } else {
                deleteConfirm = true;
                notifyWindows();
            }
        } else if (clickType.isLeftClick()) {
            if (deleteConfirm) {
                // cancel deletion on left-click
                deleteConfirm = false;
                notifyWindows();
            } else {
                teleport(player);
            }
        }
    }

    private void teleport(Player player) {
        if (home == null) {
            return;
        }
        player.closeInventory();
        home.teleport(player);
    }

    private void delete(Player player) {
        Home toDelete = home;
        if (toDelete == null) return;

        gui.homeManager().deleteHome(toDelete).thenRun(() ->
                player.getScheduler().run(EssentialsPlugin.instance(), s -> {
                    home = null;
                    notifyWindows();
                }, null)
        ).exceptionally(ex -> {
            EssentialsPlugin.instance().getSLF4JLogger().error("Could not delete home {}", toDelete, ex);
            return null;
        });
    }


    /**
     * Does not update the view.
     *
     * @param home home to display, or null for empty/undefined state
     * @return this item
     */
    public ItemHome setHome(@Nullable Home home) {
        this.home = home;
        this.deleteConfirm = false;
        return this;
    }

    /**
     * Does not update the view.
     *
     * @param locked whether this slot is locked (player cannot unlock more homes)
     * @return this item
     */
    public ItemHome setLocked(boolean locked) {
        this.locked = locked;
        return this;
    }
}
