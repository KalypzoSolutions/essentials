package de.kalypzo.essentials.gui.warps;

import de.kalypzo.essentials.EssentialsPlugin;
import de.kalypzo.essentials.exception.BadConfigurationException;
import de.kalypzo.essentials.util.Text;
import de.kalypzo.essentials.world.warps.WarpManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.structure.Structure;
import xyz.xenondevs.invui.window.Window;

import java.util.List;

/**
 * Displays all available warps in a configurable GUI.
 */
public class GuiWarps {

    private final Player player;
    private final WarpConfiguration config;
    private final Gui gui;

    public GuiWarps(Player player, WarpManager warpManager, WarpConfiguration config) throws BadConfigurationException {
        this.player = player;
        this.config = config;

        List<String> layout = config.guiLayout();
        List<ConfigurationSection> items = config.guiItems();

        Structure structure;
        try {
            structure = new Structure(layout.toArray(new String[0]));
            for (int i = 0; i < items.size(); i++) {
                char key = (char) ('1' + i);
                ConfigurationSection section = items.get(i);
                structure.addIngredient(key, new ItemWarp(section, warpManager));
            }
        } catch (Exception ex) {
            throw new BadConfigurationException("layout", "warps.yml", ex);
        }

        gui = Gui.of(structure);
    }

    public void open() {
        player.getScheduler().run(EssentialsPlugin.instance(), s -> {
            Window.single()
                    .setGui(gui)
                    .setTitle(Text.gui(config.guiTitle()))
                    .open(player);
        }, null);
    }
}
