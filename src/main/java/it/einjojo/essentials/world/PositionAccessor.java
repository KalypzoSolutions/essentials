package it.einjojo.essentials.world;

import com.google.gson.Gson;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import it.einjojo.essentials.EssentialsPlugin;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Singleton
 *
 * @author einjojo
 */
@Getter
@Slf4j
public class PositionAccessor {
    private static final String REQUEST_CHANNEL = "pos:req";
    private static final String RESPONSE_CHANNEL = "pos:resp";
    private final Map<Integer, CompletableFuture<NetworkPosition>> openRequests = new ConcurrentHashMap<>();
    private final String serverName;
    private final Gson gson = new Gson();
    private final ThreadLocalRandom random = ThreadLocalRandom.current();
    private StatefulRedisPubSubConnection<String, String> pubSubConnection;

    private PositionAccessor(@NonNull String serverName) {
        this.serverName = serverName;
    }

    private static class OnDemand {
        public static final PositionAccessor INSTANCE = new PositionAccessor(EssentialsPlugin.environment().getServerName());
    }

    public static PositionAccessor getInstance() {
        return OnDemand.INSTANCE;
    }

    public void init(StatefulRedisPubSubConnection<String, String> pubSubConnection) {
        this.pubSubConnection = pubSubConnection;
        pubSubConnection.addListener(new RedisPubSubAdapter<>() {
            @Override
            public void message(String channel, String message) {
                if (channel.equals(REQUEST_CHANNEL)) {
                    var args = message.split("::");
                    int id = Integer.parseInt(args[0]);
                    UUID uuid = UUID.fromString(args[1]);
                    Player player = Bukkit.getPlayer(uuid);
                    if (player == null) {
                        return;
                    }
                    var pos = NetworkPosition.fromLocation(player.getLocation(), serverName);
                    pubSubConnection.async().publish(RESPONSE_CHANNEL, id + "::" + gson.toJson(pos));
                } else if (channel.equals(RESPONSE_CHANNEL)) {
                    var args = message.split("::");
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
     * @param player Die UUID des Spielers dessen Position abgefragt werden soll.
     * @return Eine CompletableFuture, die die Position des Spielers enthält oder fehlschlägt.
     */
    public CompletableFuture<NetworkPosition> getPosition(UUID player) {
        var localPlayer = Bukkit.getPlayer(player);
        if (localPlayer == null) {
            if (pubSubConnection == null) {
                throw new IllegalStateException("PositionAccessor has not been initialized yet. Call initialize and provide a pub sub method");
            }
            int reqId = random.nextInt();
            pubSubConnection.async().publish(REQUEST_CHANNEL, reqId + "::" + player);
            var future = new CompletableFuture<NetworkPosition>().orTimeout(3, TimeUnit.SECONDS);
            openRequests.put(reqId, future);
            return future;
        } else {
            return CompletableFuture.completedFuture(NetworkPosition.fromLocation(localPlayer.getLocation(), serverName));
        }

    }

}
