package de.kalypzo.essentials.user.tpa;

import de.kalypzo.essentials.environment.PluginEnvironment;
import de.kalypzo.essentials.user.EssentialsUser;
import de.kalypzo.essentials.world.TeleportResult;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public record TpaRequest(UUID sender, UUID receiver) {

    public CompletableFuture<Optional<EssentialsUser>> getSenderPlayer() {
        return PluginEnvironment.getInstance().getUser(sender);
    }

    public CompletableFuture<Optional<EssentialsUser>> getReceiverPlayer() {
        return PluginEnvironment.getInstance().getUser(receiver);
    }


    /**
     * @return a future containing the teleport result
     */
    public CompletableFuture<TeleportResult> fulfill() {
        return getReceiverPlayer().thenCompose(optReceiver -> {
            if (optReceiver.isEmpty()) {
                return CompletableFuture.completedFuture(TeleportResult.DESTINATION_PLAYER_OFFLINE);
            }
            var receiver = optReceiver.get();
            return getSenderPlayer().thenCompose(optSender -> {
                if (optSender.isEmpty()) {
                    return CompletableFuture.completedFuture(TeleportResult.TELEPORTING_PLAYER_OFFLINE);
                }
                var sender = optSender.get();
                return internalTeleportPlayers(sender, receiver);
            });
        });
    }

    private CompletableFuture<TeleportResult> internalTeleportPlayers(EssentialsUser sender, EssentialsUser receiver) {
        TpaManager.getInstance().removeRequest(this.sender, this.receiver);

        return sender.teleport(receiver);
    }

}
