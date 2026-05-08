package de.kalypzo.essentials.command.user;

import de.kalypzo.essentials.EssentialsPlugin;
import de.kalypzo.essentials.user.back.BackManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.translation.Argument;
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
            return;
        }
        if (!source.hasPermission(COST_BYPASS_PERMISSION)) {
            EssentialsPlugin.instance().economyService().withdraw(source.getUniqueId(), EssentialsPlugin.instance().getConfig().getDouble("back.cost", 500), "ESSENTIALS BACK").thenAccept(result -> {
                if (!result.isSuccess()) {
                    source.sendMessage(Component.translatable("essentials.back.cost-failed"));
                    return;
                }
                source.sendMessage(Component.translatable("essentials.back.cost-success", Argument.numeric("cost", result.change())));
                BackManager.getInstance().teleportBack(source);
            });
            return;

        }
        BackManager.getInstance().teleportBack(source);
    }
}
