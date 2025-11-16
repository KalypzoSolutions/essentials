package net.wandoria.essentials.user.tpa;

import net.wandoria.essentials.environment.PluginEnvironment;
import net.wandoria.essentials.user.EssentialsUser;
import net.wandoria.essentials.world.TeleportResult;

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
        return CompletableFuture.supplyAsync(() -> {
            var optReceiver = getReceiverPlayer().join();
            if (optReceiver.isEmpty()) {
                return TeleportResult.DESTINATION_PLAYER_OFFLINE;
            }
            var optSender = getSenderPlayer().join();
            if (optSender.isEmpty()) {
                return TeleportResult.TELEPORTING_PLAYER_OFFLINE;
            }
            return internalTeleportPlayers(optSender.get(), optReceiver.get()).join();
        });
    }

    private CompletableFuture<TeleportResult> internalTeleportPlayers(EssentialsUser sender, EssentialsUser receiver) {
        TpaManager.getInstance().removeRequest(this.sender, this.receiver);

        return sender.teleport(receiver);
    }

}
