package net.wandoria.essentials.world;

import com.google.gson.Gson;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.wandoria.essentials.EssentialsPlugin;
import net.wandoria.essentials.environment.PluginEnvironment;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * <p>Allows cross-server teleports.</p>
 * <p>Singleton</p>
 * <p>Uses Redis PubSub to communicate with other servers.</p>
 * <p>The TeleportExecutor has to be {@link #init(StatefulRedisPubSubConnection)} before it can be used by the plugin main.</p>
 *
 *
 *
 */
@Slf4j
public class TeleportExecutor implements Listener {

    private static final String CHANNEL = "teleport-announce";
    public static final long EXPIRY_MILLIS = 3000;
    private final Gson gson = new Gson();
    private final LinkedList<TeleportAnnounce> pendingTeleports = new LinkedList<>();
    private final PluginEnvironment environment;
    private StatefulRedisPubSubConnection<String, String> connection;
    private final String currentServerName;

    protected TeleportExecutor(EssentialsPlugin plugin) {
        this.environment = plugin.getEnvironment();
        this.currentServerName = environment.getServerName();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private static class OnDemand {
        private static final TeleportExecutor INSTANCE = new TeleportExecutor(EssentialsPlugin.instance());
    }

    public static TeleportExecutor getInstance() {
        return OnDemand.INSTANCE;
    }


    /**
     * Initializes the TeleportExecutor with a Redis PubSub connection and binds resp. logic to it.
     *
     * @param pubSubConnection Redis PubSub Connection to use for teleport requests.
     * @throws IllegalStateException if the TeleportExecutor has already been initialized.
     */
    public void init(@NotNull StatefulRedisPubSubConnection<String, String> pubSubConnection) {
        if (this.connection != null) {
            throw new IllegalStateException("Already initialized");
        }
        this.connection = pubSubConnection;
        pubSubConnection.addListener(new RedisPubSubAdapter<>() {
            @Override
            public void message(String channel, String message) {
                if (!channel.equals(CHANNEL)) return;
                try {
                    TeleportAnnounce announce = gson.fromJson(message, TeleportAnnounce.class);
                    if (announce == null) return;
                    if (!announce.destinationServer().equals(currentServerName)) return;
                    pendingTeleports.add(announce);
                    log.info("Received teleport announce for {}", announce.target);
                } catch (Exception ex) {
                    log.error("Error while handling teleport announce", ex);
                }
            }
        });
        pubSubConnection.async().subscribe(CHANNEL);
    }


    @EventHandler(priority = EventPriority.MONITOR)
    private void executePendingTeleports(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        pendingTeleports.removeIf(teleportAnnounce -> {
            if (teleportAnnounce.isExpired()) return true;
            if (!teleportAnnounce.player().equals(player.getUniqueId())) {
                return false;
            }
            teleportAnnounce.positionOrTarget(
                    position -> tpPlayerToPositionLocally(player, position),
                    target -> tpPlayerToPlayerLocally(player, target)
            );
            log.info("Executed teleport announcement for {} ", player.getUniqueId());
            return true;
        });
    }


    /**
     * @param player does not need to be on the executing server
     * @param target does not need to be on the executing server
     */
    public void teleportPlayerToPlayer(UUID player, UUID target) {
        Player sender = Bukkit.getPlayer(player);
        Player targetPlayer = Bukkit.getPlayer(target);
        if (sender != null && targetPlayer != null) { // both on same server
            tpPlayerToPlayerLocally(sender, target);
            return;
        }
        var optTarget = environment.getUser(target).join();
        if (optTarget.isEmpty()) {
            return;
        }
        String targetServerName = optTarget.get().getServerName();
        connection.async().publish(CHANNEL, gson.toJson(new TeleportAnnounce(player, targetServerName, null, target, System.currentTimeMillis() + EXPIRY_MILLIS)));
        environment.connectPlayerToServer(player, targetServerName);
        log.info("Teleporting player {} to player {}", player, optTarget.get().getName());

    }

    public void teleportPlayerToPosition(UUID player, NetworkPosition position) {
        Player sender = Bukkit.getPlayer(player);
        if (sender != null && currentServerName.equals(position.serverName())) {
            tpPlayerToPositionLocally(sender, position);
            return;
        }
        connection.async().publish(CHANNEL, gson.toJson(new TeleportAnnounce(player, position.serverName(), position, null, System.currentTimeMillis() + EXPIRY_MILLIS)));
        environment.connectPlayerToServer(player, position.serverName());
        log.info("Teleporting player {} to position {}", player, position);
    }

    /**
     * Thread safe
     *
     * @param player   player
     * @param position destination
     */
    private void tpPlayerToPositionLocally(Player player, NetworkPosition position) {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(EssentialsPlugin.instance(), () -> tpPlayerToPositionLocally(player, position));
            return;
        }
        player.teleportAsync(position.toLocation());
    }

    /**
     * Thread safe
     * If the target is not online, nothing will happen.
     *
     * @param player player
     * @param target destination
     */
    private void tpPlayerToPlayerLocally(@NotNull Player player, @NonNull UUID target) {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(EssentialsPlugin.instance(), () -> tpPlayerToPlayerLocally(player, target));
            return;
        }
        Player targetPlayer = Bukkit.getPlayer(target);
        if (targetPlayer == null) {
            log.warn("Player {} tried to teleport to an offline player {}.", player.getName(), target);
            return;
        }
        player.teleport(targetPlayer);

    }


    /**
     * @param player   der Spieler, der teleportiert werden soll
     * @param position null oder die Position, zu der der Spieler teleportiert werden soll
     * @param target   null oder der Spieler, zu dem der Spieler teleportiert werden soll
     */
    public record TeleportAnnounce(@NonNull UUID player,
                                   String destinationServer,
                                   NetworkPosition position,
                                   UUID target, long expiresAt) {
        public void positionOrTarget(Consumer<NetworkPosition> positionConsumer, Consumer<UUID> targetConsumer) {
            if (position != null) {
                positionConsumer.accept(position);
            } else if (target != null) {
                targetConsumer.accept(target);
            } else {
                throw new IllegalStateException("No position or target present");
            }
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }


    }
}
