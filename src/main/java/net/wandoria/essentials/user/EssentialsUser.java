package net.wandoria.essentials.user;


import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.wandoria.essentials.EssentialsPlugin;
import net.wandoria.essentials.chat.ChatMessage;
import net.wandoria.essentials.util.TagResolvers;
import net.wandoria.essentials.world.NetworkPosition;
import net.wandoria.essentials.world.PositionAccessor;
import net.wandoria.essentials.world.TeleportExecutor;
import net.wandoria.essentials.world.TeleportResult;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;


/**
 * EssentialsUser represents a player which is online and supports more actions like teleport and messaging.
 * <p>Instantiated by {@link net.wandoria.essentials.environment.PluginEnvironment}</p>
 *
 * <p>It's an abstract class to adapt different underlying player apis which provide information about the user</p>
 */
@Getter
public abstract class EssentialsUser implements EssentialsOfflineUser, ComponentLike {
    private final EssentialsPlugin plugin = EssentialsPlugin.instance();
    private final UUID uuid;
    private final String name;

    public EssentialsUser(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    @Nullable
    public abstract String getServerName();

    public abstract Duration getPlayTime();

    public CompletableFuture<NetworkPosition> getPosition() {
        return PositionAccessor.getInstance().getPosition(uuid);
    }

    public void connect(String serverName) {
        plugin.getEnvironment().connectPlayerToServer(uuid, serverName);
    }

    @CanIgnoreReturnValue
    public CompletableFuture<TeleportResult> teleport(NetworkPosition position) {
        getTeleportExecutor().teleportPlayerToPosition(uuid, position);
        return CompletableFuture.completedFuture(TeleportResult.SUCCESS); //TODO
    }

    @CanIgnoreReturnValue
    public CompletableFuture<TeleportResult> teleport(EssentialsUser target) {
        getTeleportExecutor().teleportPlayerToPlayer(uuid, target.uuid);
        return CompletableFuture.completedFuture(TeleportResult.SUCCESS); //TODO
    }

    /**
     * Execute actions depending on whether the player is online on the server
     *
     * @param onlineConsumer    action to execute if the player is online locally (bukkit player)
     * @param otherServerRunner action to execute if the player is on another server
     */
    public void ifOnlineLocallyOrElse(Consumer<Player> onlineConsumer, Runnable otherServerRunner) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            onlineConsumer.accept(player);
        } else {
            otherServerRunner.run();
        }

    }

    public boolean hasDisabledPrivateMessages() {
        return plugin.getChatSystem().hasDisabledPrivateMessages(uuid);
    }


    /**
     * Sendet eine Nachricht netzwerkübergreifend nur an den Spieler, wenn der Spieler nicht auf dem Server ist.
     * Es gibt keine garantie für die Zustellung der Nachricht.
     *
     * @param component die Nachricht
     */
    public void sendMessage(Component component) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            player.sendMessage(component);
            return;
        }
        ChatMessage.create(component, List.of(uuid)).deliver(plugin.getChatSystem());
    }

    public TeleportExecutor getTeleportExecutor() {
        return TeleportExecutor.getInstance();
    }

    /**
     * <pre>Creates a tag resolver for player</pre>
     *
     * @return <pre>&lt;player&gt; resolver</pre>
     */
    public TagResolver playerTagResolver() {
        return TagResolvers.player(this);
    }

    @Override
    public @NotNull Component asComponent() {
        return Component.text(name).hoverEvent(Component.text(uuid.toString()));
    }

    public void playSound(Sound sound, float volume, float pitch) {
        ifOnlineLocallyOrElse(
                player -> player.playSound(player.getLocation(), sound, volume, pitch),
                () -> {

                }
        );
    }
}
