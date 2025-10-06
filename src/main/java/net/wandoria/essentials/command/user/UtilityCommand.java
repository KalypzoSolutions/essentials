package net.wandoria.essentials.command.user;

import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.paper.util.sender.PlayerSource;

/**
 * <p>Workbench, Anvil</p>
 * <p>Because of @CommandContainer it gets instantiated by {@link net.wandoria.essentials.command.CommandManager}</p>
 */
@CommandContainer
public class UtilityCommand {

    @Command("workbench|wb|craft")
    @CommandDescription("Öffne die Werkbank")
    @Permission("essentials.workbench")
    public void workbench(PlayerSource source) {
        InventoryView view = MenuType.CRAFTING.builder().build(source.source());
        view.open();
    }


}
