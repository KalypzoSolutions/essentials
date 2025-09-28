package net.wandoria.essentials.user;


import java.time.Duration;
import java.util.UUID;

/**
 * EssentialsUser represents a player which is online and supports more actions like teleport and messaging.
 * It can be obtained using
 */
public interface EssentialsOfflineUser {


    String getName();

    UUID getUuid();

    Duration getPlayTime();

    default UUID getUniqueId() {
        return getUuid();
    }


}
