package net.wandoria.essentials.util;

import lombok.experimental.UtilityClass;
import net.wandoria.essentials.EssentialsPlugin;
import org.bukkit.Utility;
import org.bukkit.entity.Player;

import java.util.concurrent.Executor;

/**
 * Creates an executor for the player
 */
@UtilityClass
public class PlayerExecutorFactory {

    public static Executor createExecutor(Player player) {
        return task -> {
            player.getScheduler().run(EssentialsPlugin.instance(), (scheduler) -> {
                task.run();
            }, () -> {

            });
        };
    }

}
