package net.wandoria.essentials.user;


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.UUID;

/**
 * EssentialsUser represents a player which is online and supports more actions like teleport and messaging.
 * It can be obtained using
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
