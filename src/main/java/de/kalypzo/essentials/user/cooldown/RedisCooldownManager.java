package de.kalypzo.essentials.user.cooldown;

import io.lettuce.core.SetArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.jetbrains.annotations.Blocking;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.UUID;

/**
 * Uses Redis to manage cooldowns
 */
@Blocking
public class RedisCooldownManager {
    private static final String KEY_SPACE = "essentials:cooldowns";
    // We store the namespace as part of the key prefix for faster lookups
    private final StatefulRedisConnection<String, String> connection;
    private final String namespace;

    public RedisCooldownManager(StatefulRedisConnection<String, String> connection, String namespace) {
        this.connection = connection;
        this.namespace = namespace;
    }

    /**
     * Helper to construct a consistent Redis key.
     * Format: essentials:cooldowns:namespace:uuid
     */
    private String makeKey(UUID uuid) {
        return KEY_SPACE + ":" + this.namespace + ":" + uuid.toString();
    }

    /**
     * Sets a cooldown for the specific UUID.
     * If the duration is null, zero, or negative, the cooldown is removed.
     *
     * @param uuid     The player/entity UUID.
     * @param duration The duration of the cooldown.
     */
    public void setCooldown(UUID uuid, @Nullable Duration duration) {
        RedisCommands<String, String> commands = connection.sync();
        String key = makeKey(uuid);
        // If duration is null or <= 0, we treat it as clearing the cooldown
        if (duration == null || duration.isNegative() || duration.isZero()) {
            commands.del(key);
            return;
        }
        commands.set(key, "1", SetArgs.Builder.px(duration.toMillis()));
    }

    public void clearCooldown(UUID uuid) {
        setCooldown(uuid, null);
    }

    /**
     * Gets the remaining cooldown duration.
     *
     * @param uuid The player/entity UUID.
     * @return The remaining Duration, or null if no cooldown is active.
     */
    public @Nullable Duration getCooldown(UUID uuid) {
        RedisCommands<String, String> commands = connection.sync();
        String key = makeKey(uuid);

        // pttl returns the remaining time to live in milliseconds.
        // Returns -2 if the key does not exist.
        // Returns -1 if the key exists but has no associated expire.
        Long ttlMillis = commands.pttl(key);

        // If ttl is null (connection error) or negative (key doesn't exist/expired), return null.
        if (ttlMillis == null || ttlMillis < 0) {
            return null;
        }

        return Duration.ofMillis(ttlMillis);
    }
}