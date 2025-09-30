package net.wandoria.essentials.user;

import it.einjojo.playerapi.NetworkPlayer;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

import java.time.Duration;

@NullMarked
public class NetworkEssentialsUser extends EssentialsUser {
    public final NetworkPlayer networkPlayer;

    public NetworkEssentialsUser(NetworkPlayer player) {
        super(player.getUniqueId(), player.getName());
        this.networkPlayer = player;
    }

    /**
     * Gets the server name where the player was connected to at the moment the object has been created.
     *
     * @return server Name or null if the player is not connected to a server.
     */
    @Override
    public @Nullable String getServerName() {
        return networkPlayer.getConnectedServerName().orElse(null);
    }


    @Override
    public Duration getPlayTime() {
        return Duration.ofMillis(networkPlayer.getSessionTime());
    }
}
