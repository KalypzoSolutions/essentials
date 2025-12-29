package net.wandoria.essentials.rce;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Payload class for rce
 *
 * @param serverName     the target server
 * @param command        without /
 * @param playerExecutor if not null, the player will execute the command if he is online on the server.
 */
public record RemoteCommandCall(@NotNull String serverName, @NotNull String command,
                                @Nullable UUID playerExecutor) {

    public static RemoteCommandCall console(String serverName, String command) {
        return new RemoteCommandCall(serverName, command, null);
    }

    public static RemoteCommandCall player(UUID player, String serverName, String command) {
        return new RemoteCommandCall(serverName, command, player);
    }
    public void executeWhenOnline() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void executeNow() {
        RemoteCommandExecutor.getInstance().execute(this);
    }

}
