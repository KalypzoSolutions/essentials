package net.wandoria.essentials.user.back;

import io.lettuce.core.api.StatefulRedisConnection;
import net.wandoria.essentials.EssentialsPlugin;
import net.wandoria.essentials.world.NetworkPosition;
import net.wandoria.essentials.world.TeleportExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

/**
 * Singleton class for managing player death locations.
 * <p>To fully work, the plugin must register {@link DeathListener}</p>
 */
public class BackManager {
    private static final int BACK_TTL_SECONDS = 60 * 30;
    private static final String BACK_LOCATION_KEY_PREFIX = "back:";
    private final StatefulRedisConnection<String, String> redisConnection;
    private final TeleportExecutor teleportExecutor;


    private BackManager(@NonNull StatefulRedisConnection<String, String> statefulRedisConnection, @NonNull TeleportExecutor teleportExecutor) {
        this.redisConnection = statefulRedisConnection;
        this.teleportExecutor = teleportExecutor;
    }

    private static class OnDemand {
        private static final BackManager INSTANCE = new BackManager(EssentialsPlugin.instance().getRedis().connect(), TeleportExecutor.getInstance());
    }

    public static BackManager getInstance() {
        return OnDemand.INSTANCE;
    }

    public boolean hasBackLocation(@NotNull UUID uuid) {
        return getBackLocation(uuid) != null;
    }

    public @Nullable NetworkPosition getBackLocation(@NotNull UUID uuid) {
        var backLocation = redisConnection.sync().get(BACK_LOCATION_KEY_PREFIX + uuid);
        if (backLocation == null) {
            return null;
        }
        return NetworkPosition.deserialize(backLocation);
    }

    public void removeBackLocation(@NotNull UUID uuid) {
        redisConnection.sync().del(BACK_LOCATION_KEY_PREFIX + uuid);
    }

    /**
     * Fails silently if no back location is set.
     *
     * @param player player to teleport to
     *
     */
    public void teleportBack(@NotNull Player player) {
        NetworkPosition pos = getBackLocation(player.getUniqueId());
        if (pos == null) {
            return;
        }
        removeBackLocation(player.getUniqueId());
        teleportExecutor.teleportPlayerToPosition(player.getUniqueId(), pos);
    }

    /**
     * Sets the death location for a player.
     *
     * @param uuid     player's uuid
     * @param position the position to set
     */
    public void setDeathLocation(@NotNull UUID uuid, @NotNull NetworkPosition position) {
        redisConnection.sync().setex(BACK_LOCATION_KEY_PREFIX + uuid, BACK_TTL_SECONDS, position.serialize());
    }


}
