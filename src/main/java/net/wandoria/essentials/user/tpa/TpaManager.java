package net.wandoria.essentials.user.tpa;


import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.lettuce.core.Range;
import io.lettuce.core.api.StatefulRedisConnection;
import net.wandoria.essentials.EssentialsPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Accessor class to the redis database
 */
public class TpaManager {
    public static final int TPA_TTL_SECONDS = 300; // 5 Minutes
    private static final String KEYSPACE = "essentials:tpa:";
    private final StatefulRedisConnection<String, String> redisConnection;

    private TpaManager() {
        redisConnection = EssentialsPlugin.instance().getRedis().connect();
    }

    private static class OnDemand {
        private final static TpaManager INSTANCE = new TpaManager();
    }

    public static TpaManager getInstance() {
        return OnDemand.INSTANCE;
    }

    /**
     * Creates a request in database and delivers the messages.
     *
     * @param requester requesting user
     * @param target    the target user
     * @return future that completes when the request has been delivered. The caller must handle exceptions.
     */
    @CanIgnoreReturnValue
    public CompletableFuture<TpaRequest> create(@NotNull UUID requester, @NotNull UUID target) {
        long expirationTime = System.currentTimeMillis() + (TPA_TTL_SECONDS * 1000);
        String requesterKey = KEYSPACE + requester;
        return redisConnection.async().zadd(requesterKey, expirationTime, target.toString())
                .thenCompose(((l) -> redisConnection.async().expire(requesterKey, TPA_TTL_SECONDS + 60)))
                .thenApply(_any -> new TpaRequest(requester, target))
                .toCompletableFuture();
    }

    /**
     * Get a request from redis
     *
     * @param requester requester of the request
     * @param target    target of the request
     * @return an optional containing the TpaRequest if it exists and is still valid
     */
    public Optional<TpaRequest> getRequest(@NotNull UUID requester, @NotNull UUID target) {
        String key = KEYSPACE + requester;
        long now = System.currentTimeMillis();
        redisConnection.sync().zremrangebyscore(key, Range.create(0, now));
        Double score = redisConnection.sync().zscore(key, target.toString());
        boolean hasRequest = score != null && score > now;
        if (hasRequest) {
            return Optional.of(new TpaRequest(requester, target));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Only called by inside the TpaRequest class
     *
     * @param requester requester of the request
     * @param target    target of the request
     *
     */
    protected void removeRequest(@NotNull UUID requester, @NotNull UUID target) {
        String key = KEYSPACE + requester;
        redisConnection.sync().zrem(key, target.toString());
    }


}
