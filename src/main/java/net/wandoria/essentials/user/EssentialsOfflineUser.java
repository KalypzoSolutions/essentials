package net.wandoria.essentials.user;


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.UUID;

/**
 * EssentialsUser represents a player which is online and supports more actions like teleport and messaging.
 * <p>Instantiated by {@link net.wandoria.essentials.environment.PluginEnvironment}</p>
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
