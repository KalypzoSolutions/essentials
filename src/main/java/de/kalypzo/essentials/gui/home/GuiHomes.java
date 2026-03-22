package de.kalypzo.essentials.gui.home;

import de.kalypzo.essentials.EssentialsPlugin;
import de.kalypzo.essentials.exception.BadConfigurationException;
import de.kalypzo.essentials.user.home.Home;
import de.kalypzo.essentials.user.home.HomeManager;
import de.kalypzo.essentials.util.Text;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.gui.structure.Structure;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Displays all available homes of a player.
 */
public class GuiHomes {

    private final HomeManager homeManager;
    private final Player player;
    private final Gui gui;
    private final List<ItemHome> homeItems = new ArrayList<>();
    private final CompletableFuture<List<Home>> homeFuture;

    public GuiHomes(Player player, HomeManager homeManager) throws BadConfigurationException {
        this.homeManager = homeManager;
        this.player = player;

        List<String> layout = homeManager.config().guiLayout();

        // Build structure – use a SimpleItem placeholder for '1' so InvUI can
        // calculate the GUI size; we replace those slots manually right after.
        Structure structure;
        try {
            structure = new Structure(layout.toArray(new String[0]));
            structure.addIngredient('0', new SimpleItem(ItemProvider.EMPTY));
            structure.addIngredient('1', Markers.CONTENT_LIST_SLOT_HORIZONTAL);
            structure.addIngredient('i', new ItemHomeInfo(this));
        } catch (Exception ex) {
            throw new BadConfigurationException("layout", "homes.yml", ex);
        }
        gui = Gui.of(structure);


        for (int slot : structure.getIngredientList().findIndicesOfMarker(Markers.CONTENT_LIST_SLOT_HORIZONTAL)) {
            var item = new ItemHome(this);
            homeItems.add(item);
            gui.setItem(slot, item);
        }
        // Load homes asynchronously, then update items on the main thread
        homeFuture = homeManager.getHomes(player.getUniqueId());
        int maxHomes = homeManager.getMaxHomes(player);

        homeFuture.thenAccept(homes -> {
            for (int i = 0; i < homeItems.size(); i++) {
                ItemHome item = homeItems.get(i);
                if (i < homes.size()) {
                    item.setHome(homes.get(i));
                } else if (i >= maxHomes) {
                    item.setLocked(true);
                }
                // else: slot is unlocked but no home set – shows as undefined
            }
            player.getScheduler().run(EssentialsPlugin.instance(), s -> {
                for (ItemHome item : homeItems) {
                    item.notifyWindows();
                }
            }, null);
        }).exceptionally(ex -> {
            EssentialsPlugin.instance().getSLF4JLogger().error("Could not load homes for GUI", ex);
            player.sendMessage(Text.deserialize("<prefix> <ex>Ein Fehler ist aufgetreten."));
            return null;
        });
    }

    public void open() {
        player.getScheduler().run(EssentialsPlugin.instance(), s -> {
            Window.single()
                    .setGui(gui)
                    .setTitle(Text.gui(homeManager.config().guiTitle()))
                    .open(player);
        }, null);
    }

    public HomeManager homeManager() {
        return homeManager;
    }

    public CompletableFuture<List<Home>> homeFuture() {
        return homeFuture;
    }
}
