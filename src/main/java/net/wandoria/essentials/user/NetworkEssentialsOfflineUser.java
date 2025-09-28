package net.wandoria.essentials.user;

import it.einjojo.playerapi.OfflineNetworkPlayer;

import java.time.Duration;
import java.util.UUID;

public class NetworkEssentialsOfflineUser implements EssentialsOfflineUser {
    private final OfflineNetworkPlayer offlineNetworkPlayer;

    public NetworkEssentialsOfflineUser(OfflineNetworkPlayer offlineNetworkPlayer) {
        this.offlineNetworkPlayer = offlineNetworkPlayer;
    }

    @Override
    public Duration getPlayTime() {
        return Duration.ofMillis(offlineNetworkPlayer.getPlaytime());
    }

    @Override
    public String getName() {
        return offlineNetworkPlayer.getName();
    }

    @Override
    public UUID getUuid() {
        return offlineNetworkPlayer.getUniqueId();
    }


}
