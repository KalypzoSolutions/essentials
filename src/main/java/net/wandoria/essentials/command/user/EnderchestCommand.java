package net.wandoria.essentials.command.user;


import net.kyori.adventure.text.Component;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.bukkit.data.SinglePlayerSelector;
import org.incendo.cloud.paper.util.sender.PlayerSource;

/**
 * <p>Because of @CommandContainer it gets instantiated by {@link net.wandoria.essentials.command.CommandManager}</p>
 */
@CommandContainer
public class EnderchestCommand {

    @Command("enderchest|ec")
    @Permission("mirania.central.bukkit.command.enderchest")
    @CommandDescription("Öffnet deine Enderchest")
    public void openOtherEnderchest(PlayerSource sender) {
        sender.source().openInventory(sender.source().getEnderChest());
        sender.source().sendMessage(Component.translatable("essentials.enderchest.open"));
    }

    @Command("enderchest|ec <player>")
    @Permission("mirania.central.bukkit.command.enderchest.other")
    @CommandDescription("Öffnet die Enderchest eines Spielers")
    public void openOtherEnderchest(PlayerSource sender, SinglePlayerSelector player) {
        sender.source().openInventory(player.single().getEnderChest());
    }
}
