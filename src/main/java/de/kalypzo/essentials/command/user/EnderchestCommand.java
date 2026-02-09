package de.kalypzo.essentials.command.user;


import de.kalypzo.essentials.command.CommandManager;
import org.bukkit.Sound;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.bukkit.data.SinglePlayerSelector;
import org.incendo.cloud.paper.util.sender.PlayerSource;

/**
 * <p>Because of @CommandContainer it gets instantiated by {@link CommandManager}</p>
 */
@CommandContainer
public class EnderchestCommand {

    @Command("enderchest|ec")
    @Permission("essentials.command.enderchest")
    @CommandDescription("Öffnet deine Enderchest")
    public void openOtherEnderchest(PlayerSource sender) {
        sender.source().openInventory(sender.source().getEnderChest());
        sender.source().playSound(sender.source(), Sound.BLOCK_ENDER_CHEST_OPEN, 1, 1.2f);
    }

    @Command("enderchest|ec <player>")
    @Permission("essentials.command.enderchest.other")
    @CommandDescription("Öffnet die Enderchest eines Spielers")
    public void openOtherEnderchest(PlayerSource sender, SinglePlayerSelector player) {
        sender.source().openInventory(player.single().getEnderChest());
        sender.source().playSound(sender.source(), Sound.BLOCK_ENDER_CHEST_OPEN, 1, 1.2f);
    }
}
