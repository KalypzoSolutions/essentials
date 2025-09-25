package net.wandoria.essentials.user;

import it.einjojo.playerapi.NetworkPlayer;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

public class NetworkEssentialsUser extends EssentialsUser {
    private final NetworkPlayer player;

    public NetworkEssentialsUser(NetworkPlayer player) {
        super(player.getUniqueId(), player.getName());
        this.player = player;
    }

    @Override
    public @Nullable String getServerName() {
        return player.getConnectedServerName().orElse(null);
    }

    @Override
    public Duration getPlayTime() {
        return Duration.ofMillis(player.getSessionTime());
    }
}
