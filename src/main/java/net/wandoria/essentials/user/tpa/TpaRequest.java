package net.wandoria.essentials.user.tpa;

import net.wandoria.essentials.environment.PluginEnvironment;
import net.wandoria.essentials.user.EssentialsUser;
import net.wandoria.essentials.world.TeleportResult;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public record TpaRequest(UUID sender, UUID receiver) {

    public CompletableFuture<Optional<EssentialsUser>> getSenderPlayer() {
        return PluginEnvironment.getInstance().getUser(sender);
    }

    public CompletableFuture<Optional<EssentialsUser>> getReceiverPlayer() {
        return PluginEnvironment.getInstance().getUser(sender);
    }

    /**
     * Fulfills this request and sends a message to the handle
     *
     * @param localPlayerHandle instance of the receiving player which will receive the message.
     */
    public void accept(Player localPlayerHandle) {
        if (!localPlayerHandle.getUniqueId().equals(receiver)) {
            throw new IllegalArgumentException("Player must be the same as the receiver");
        }


    }

    /**
     * @return a future containing the teleport result
     */
    public CompletableFuture<TeleportResult> fulfill() {
        return getReceiverPlayer().thenCompose(receiverOpt -> {
            if (receiverOpt.isEmpty()) {
                return CompletableFuture.completedFuture(TeleportResult.DESTINATION_PLAYER_OFFLINE);
            }

        })
    }
}
