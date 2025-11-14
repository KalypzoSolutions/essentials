package net.wandoria.essentials.user.tpa;


import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.lettuce.core.api.StatefulRedisConnection;
import net.wandoria.essentials.EssentialsPlugin;
import net.wandoria.essentials.user.EssentialsUser;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Future;

public class TpaManager {
    private static final String KEYSPACE = "essentials:tpa:";
    private final EssentialsPlugin plugin;
    private final StatefulRedisConnection<String, String> redisConnection;

    private TpaManager() {
        plugin = EssentialsPlugin.instance();
        redisConnection = plugin.getRedis().connect();
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
     * @param request requesting user
     * @param target  the target user
     * @return future that completes when the request has been delivered.
     */
    @CanIgnoreReturnValue
    public Future<TpaRequest> create(@NotNull EssentialsUser request, @NotNull EssentialsUser target) {
        redisConnection.async().setex()

    }

}
