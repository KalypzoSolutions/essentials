package net.wandoria.essentials.command.user;

import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.paper.util.sender.PlayerSource;

/**
 * <p>Because of @CommandContainer it gets instantiated by {@link net.wandoria.essentials.command.CommandManager}</p>
 */
@CommandContainer
public class WorkbenchCommand {

    @Command("workbench|wb")
    @CommandDescription("Öffnet das Handwerkstisch")
    @Permission("mirania.central.bukkit.command.workbench")
    public void openWorkBench(PlayerSource source) {
        InventoryView view = MenuType.CRAFTING.builder().build(source.source());
        view.open();
    }


}
