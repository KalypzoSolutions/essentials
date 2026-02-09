package de.kalypzo.essentials.world;

import com.google.gson.Gson;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import de.kalypzo.essentials.EssentialsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Singleton can be obtained with {@link #getInstance()}
 * <p>
 * Provides methods to get the position of a player on the server network.
 * Uses Redis PubSub to communicate with other servers.
 * The PositionAccessor has to be {@link #init(StatefulRedisPubSubConnection)} before it can be used by the plugin main.
 *
 * @author einjojo
 */
@Getter
@Slf4j
public class PositionAccessor {
    private static final String REQUEST_CHANNEL = "pos:req";
    private static final String RESPONSE_CHANNEL = "pos:resp";
    private static final String ARGUMENT_TERMINATOR = "::";
    private final Map<Integer, CompletableFuture<NetworkPosition>> openRequests = new ConcurrentHashMap<>(); // request id -> future
    private final String localServerName;
    private final Gson gson = new Gson();
    private final ThreadLocalRandom random = ThreadLocalRandom.current(); // for request ids
    private StatefulRedisPubSubConnection<String, String> pubSubConnection;

    private PositionAccessor(@NonNull String serverName) {
        this.localServerName = serverName;
    }

    private static class OnDemand {
        public static final PositionAccessor INSTANCE = new PositionAccessor(EssentialsPlugin.environment().getServerName());
    }

    public static PositionAccessor getInstance() {
        return OnDemand.INSTANCE;
    }

    /**
     * Initializes the PositionAccessor with a Redis PubSub connection and binds resp. logic to it.
     *
     * @param pubSubConnection Redis PubSub Connection to use for position requests.
     */
    public void init(StatefulRedisPubSubConnection<String, String> pubSubConnection) {
        this.pubSubConnection = pubSubConnection;
        pubSubConnection.addListener(new RedisPubSubAdapter<>() {
            @Override
            public void message(String channel, String message) {
                if (channel.equals(REQUEST_CHANNEL)) {
                    var args = message.split(ARGUMENT_TERMINATOR);
                    int id = Integer.parseInt(args[0]);
                    UUID uuid = UUID.fromString(args[1]);
                    Player player = Bukkit.getPlayer(uuid);
                    if (player == null) {
                        return;
                    }
                    var pos = NetworkPosition.fromLocation(player.getLocation(), localServerName);
                    pubSubConnection.async().publish(RESPONSE_CHANNEL, id + ARGUMENT_TERMINATOR + gson.toJson(pos));
                } else if (channel.equals(RESPONSE_CHANNEL)) {
                    var args = message.split(ARGUMENT_TERMINATOR);
                    int id = Integer.parseInt(args[0]);
                    var request = openRequests.remove(id);
                    if (request == null) {
                        return;
                    }
                    request.completeAsync(() -> gson.fromJson(args[1], NetworkPosition.class));
                    log.info("Received position response for player {}", args[1]);
                }
            }
        });
        pubSubConnection.sync().subscribe(REQUEST_CHANNEL, RESPONSE_CHANNEL);
    }


    /**
     * <p>If the player is offline, this future will time out.</p>
     * <p>If the player is online locally, the position of the entity will be returned instantly.</p>
     *
     * @param player the UUID of an online player
     * @return A completable future which completes with the position of the player or exceptionally if an error occurs with a timeout of 3 seconds.
     * @throws IllegalStateException if the PositionAccessor has not been initialized yet.
     */
    public CompletableFuture<NetworkPosition> getPosition(UUID player) {
        var localPlayer = Bukkit.getPlayer(player);
        if (localPlayer == null) {
            if (pubSubConnection == null) {
                throw new IllegalStateException("PositionAccessor has not been initialized yet. Call init(StatefulRedisPubSubConnection) and provide a pub sub method once");
            }
            int reqId = random.nextInt();
            pubSubConnection.async().publish(REQUEST_CHANNEL, reqId + "::" + player);
            var future = new CompletableFuture<NetworkPosition>().orTimeout(3, TimeUnit.SECONDS);
            openRequests.put(reqId, future);
            return future;
        } else {
            return CompletableFuture.completedFuture(NetworkPosition.fromLocation(localPlayer.getLocation(), localServerName));
        }

    }

}
