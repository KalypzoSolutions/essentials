package net.wandoria.essentials.command.user;


import net.kyori.adventure.text.Component;
import net.wandoria.essentials.user.back.BackManager;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.paper.util.sender.PlayerSource;

/**
 * When a player dies he gets teleported he can teleport back to his last death location
 * <p>Because of @CommandContainer it gets instantiated by {@link net.wandoria.essentials.command.CommandManager}</p>
 */
@CommandContainer
public class BackCommand {

    public static final String PERMISSION = "essentials.command.back";


    @Command("back")
    @CommandDescription("Nach dem Tot zurück")
    @Permission(PERMISSION)
    public void teleportBack(PlayerSource source) {
        if (!BackManager.getInstance().hasBackLocation(source.source().getUniqueId())) {
            source.source().sendMessage(Component.translatable("essentials.back.no-location"));
        }
        BackManager.getInstance().teleportBack(source.source());
    }

}
