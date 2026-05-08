package de.kalypzo.essentials.command.user;

import de.kalypzo.essentials.EssentialsPlugin;
import de.kalypzo.essentials.user.back.BackManager;
import de.kalypzo.essentials.util.PermissionsRange;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import studio.mevera.imperat.annotations.types.Description;
import studio.mevera.imperat.annotations.types.Execute;
import studio.mevera.imperat.annotations.types.Permission;
import studio.mevera.imperat.annotations.types.RootCommand;

@RootCommand("back")
@Description("Nach dem Tot zurück")
@Permission(BackCommand.PERMISSION)
public class BackCommand {
    public static final String PERMISSION = "essentials.command.back";
    public static final String COST_BYPASS_PERMISSION = "essentials.perk.back";

    @Execute
    public void teleportBack(Player source) {
        if (!BackManager.getInstance().hasBackLocation(source.getUniqueId())) {
            source.sendMessage(Component.translatable("essentials.back.no-location"));
        }
        if (!source.hasPermission(COST_BYPASS_PERMISSION)) {
            EssentialsPlugin.instance().economyService().withdraw(source.getUniqueId(), 50, "back command").thenAccept(result -> {
                if (!result.isSuccess()) {
                    source.sendMessage(Component.translatable("essentials.back.cost-failed"));
                    return;
                }
                BackManager.getInstance().teleportBack(source);
            });
            return;
        }
        BackManager.getInstance().teleportBack(source);
    }
}
