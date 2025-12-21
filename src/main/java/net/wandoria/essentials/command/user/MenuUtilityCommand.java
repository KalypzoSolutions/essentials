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
public class MenuUtilityCommand {

    @Command("workbench|wb|craft")
    @CommandDescription("Öffne die Werkbank")
    @Permission("essentials.workbench")
    public void workbench(PlayerSource source) {
        InventoryView view = MenuType.CRAFTING.builder().build(source.source());
        view.open();
    }

    @Command("loom")
    @CommandDescription("Loom")
    @Permission("essentials.loom")
    public void loom(PlayerSource source) {
        MenuType.LOOM.builder().build(source.source()).open();
    }

    @Command("anvil")
    @CommandDescription("Opens anvil")
    @Permission("essentials.anvil")
    public void anvil(PlayerSource source) {
        MenuType.ANVIL.builder().build(source.source()).open();
    }

    @Command("stonecutter")
    @CommandDescription("Opens stone cutter")
    @Permission("essentials.stonecutter")
    public void stoneCutter(PlayerSource source) {
        MenuType.STONECUTTER.builder().build(source.source()).open();
    }




}
