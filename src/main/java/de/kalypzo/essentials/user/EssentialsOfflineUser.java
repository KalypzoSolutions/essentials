package de.kalypzo.essentials.user;


import de.kalypzo.essentials.environment.PluginEnvironment;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.UUID;

/**
 * EssentialsOfflineUser represents a player which may be online or offline.
 * <p>Instantiated by {@link PluginEnvironment}</p>
 *
 * <p>It's an abstract class to adapt different underlying player apis which provide information about the user</p>
 */
public interface EssentialsOfflineUser extends ComponentLike {


    String getName();

    UUID getUuid();

    Duration getPlayTime();

    default UUID getUniqueId() {
        return getUuid();
    }

    @Override
    @NotNull Component asComponent();
}
