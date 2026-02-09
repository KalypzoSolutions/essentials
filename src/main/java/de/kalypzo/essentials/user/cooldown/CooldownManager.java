package de.kalypzo.essentials.user.cooldown;

import io.lettuce.core.api.StatefulRedisConnection;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.UUID;

/**
 * Uses Redis to manage cooldowns
 */
@Deprecated(forRemoval = true)
public class CooldownManager {
    private static final String REDIS_KEY = "essentials:cooldown:";
    private final String cooldownKey;
    private final StatefulRedisConnection<String, String> redis;

    public CooldownManager(String cooldownKey, StatefulRedisConnection<String, String> redis) {
        this.cooldownKey = cooldownKey;
        this.redis = redis;
    }

    /**
     *
     * @param uuid uuid of the player
     * @return true if the player has a cooldown
     */
    public boolean isCooldown(UUID uuid) {
        return redis.sync().get(redisKey(uuid)) != null;
    }

    /**
     * Sets a cooldown for the given player.
     *
     * @param uuid     uuid of the player
     * @param duration duration of the cooldown or null to remove it
     */
    public void setCooldown(UUID uuid, @Nullable Duration duration) {
        if (duration != null) {
            redis.sync().setex(redisKey(uuid), duration.toSeconds(), String.valueOf(System.currentTimeMillis() + duration.toMillis()));
        } else {
            redis.sync().del(redisKey(uuid));
        }
    }

    public Long getExpiryTimestamp(UUID uuid) {
        String expiry = redis.sync().get(redisKey(uuid));
        if (expiry == null) return null;
        return Long.parseLong(expiry);
    }

    /**
     * Removes the cooldown for the given player.
     *
     * @param uuid uuid of the player
     */
    public void removeCooldown(UUID uuid) {
        redis.sync().del(redisKey(uuid));
    }


    private String redisKey(UUID uuid) {
        return REDIS_KEY + cooldownKey + ":" + uuid;
    }


}
