package net.wandoria.essentials.user;


import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.wandoria.essentials.EssentialsPlugin;
import net.wandoria.essentials.chat.ChatMessage;
import net.wandoria.essentials.world.NetworkPosition;
import net.wandoria.essentials.world.PositionAccessor;
import net.wandoria.essentials.world.TeleportExecutor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * EssentialsUser represents a player which may be online or offline.
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

    public void teleport(NetworkPosition position) {
        getTeleportExecutor().teleportPlayerToPosition(uuid, position);
    }

    public void teleport(EssentialsUser target) {
        getTeleportExecutor().teleportPlayerToPlayer(uuid, target.uuid);
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

    @Override
    public @NotNull Component asComponent() {
        return Component.text(name).hoverEvent(Component.text(uuid.toString()));
    }
}
