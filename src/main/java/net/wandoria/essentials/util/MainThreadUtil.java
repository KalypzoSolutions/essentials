package net.wandoria.essentials.util;

import net.wandoria.essentials.EssentialsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.Executor;

/**
 *
 */
public class MainThreadUtil {

    public static boolean isMainThread() {
        return Bukkit.isPrimaryThread();
    }

    public static void run(Player scheduler, Runnable task) {
        scheduler.getScheduler().run(
                EssentialsPlugin.instance(),
                (s) -> task.run(),
                null
        );
    }

    public static Executor createExecutor(Player player) {
        return task -> {
            player.getScheduler().run(EssentialsPlugin.instance(), (scheduler) -> {
                task.run();
            }, () -> {

            });
        };
    }

}
