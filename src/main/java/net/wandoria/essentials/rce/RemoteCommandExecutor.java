package net.wandoria.essentials.rce;

import com.google.gson.Gson;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import lombok.extern.slf4j.Slf4j;
import net.wandoria.essentials.EssentialsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Makes the Bukkit#dispatchCommand method available to be executed remotely using {@link RemoteCommandCall}
 */
@Slf4j
@NullMarked
public class RemoteCommandExecutor {
    public static final String CHANNEL = "wandoria:rce";
    private @Nullable StatefulRedisPubSubConnection<String, String> connection;
    private final String serverName;
    private final Gson gson = new Gson();

    private RemoteCommandExecutor(String serverName) {
        this.serverName = serverName;
    }

    public void init(StatefulRedisPubSubConnection<String, String> connection) {
        this.connection = connection;
        connection.sync().subscribe(CHANNEL);
        connection.addListener(new RedisPubSubAdapter<>() {
            @Override
            public void message(String channel, String message) {
                if (!channel.equals(CHANNEL)) {
                    return;
                }
                try {
                    executeLocally(gson.fromJson(message, RemoteCommandCall.class));
                } catch (Exception ex) {
                    log.error("Error while handling remote command call", ex);
                }
            }
        });
    }

    private static class onDemand {
        private static final RemoteCommandExecutor INSTANCE = new RemoteCommandExecutor(EssentialsPlugin.environment().getServerName());
    }

    public static RemoteCommandExecutor getInstance() {
        return onDemand.INSTANCE;
    }

    public void execute(RemoteCommandCall command) {
        if (connection == null) {
            throw new IllegalStateException("Connection is not initialized yet");
        }
        if (command.serverName().equals(serverName)) {
            executeLocally(command);
            return;
        }
        connection.sync().publish(CHANNEL, gson.toJson(command));
    }

    /**
     * <p>returns silently if the command is not for this server</p>
     * <p>returns silently if the player, who is provided as executor, is not online.</p>
     * <p>Runs the command on the main thread.</p>
     *
     * @param command any command
     */
    protected void executeLocally(RemoteCommandCall command) {
        if (!command.serverName().equals(serverName)) {
            return;
        }
        EssentialsPlugin.instance().getServer().getScheduler().runTask(EssentialsPlugin.instance(), () -> {
            CommandSender sender = command.playerExecutor() == null ? Bukkit.getServer().getConsoleSender() : Bukkit.getPlayer(command.playerExecutor());
            if (sender == null) {
                return;
            }
            Bukkit.getServer().dispatchCommand(
                    sender,
                    command.command()
            );
            log.info("Executed remote command calll '{}' as {}", command.command(), sender.getName());
        });
    }


}
