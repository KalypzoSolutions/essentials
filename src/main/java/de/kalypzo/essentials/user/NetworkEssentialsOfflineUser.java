package de.kalypzo.essentials.user;

import it.einjojo.playerapi.OfflineNetworkPlayer;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.UUID;

/**
 * Implementation using the PLAYER-API
 */
public class NetworkEssentialsOfflineUser implements EssentialsOfflineUser {
    private final OfflineNetworkPlayer offlineNetworkPlayer;


    public NetworkEssentialsOfflineUser(OfflineNetworkPlayer offlineNetworkPlayer) {
        this.offlineNetworkPlayer = offlineNetworkPlayer;
    }

    @Override
    public Duration getPlayTime() {
        if (offlineNetworkPlayer.isOnline()) {
            return Duration.ofMillis(offlineNetworkPlayer.getPlaytime() + System.currentTimeMillis() - offlineNetworkPlayer.getLastPlayed() - offlineNetworkPlayer.getAfkDuration());
        }
        return Duration.ofMillis(offlineNetworkPlayer.getPlaytime() - offlineNetworkPlayer.getAfkDuration());
    }

    @Override
    public String getName() {
        return offlineNetworkPlayer.getName();
    }

    @Override
    public UUID getUuid() {
        return offlineNetworkPlayer.getUniqueId();
    }


    @Override
    public @NotNull Component asComponent() {
        return Component.text(getName());
    }
}
