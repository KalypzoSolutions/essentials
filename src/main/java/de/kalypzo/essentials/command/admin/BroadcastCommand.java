package de.kalypzo.essentials.command.admin;

import de.kalypzo.essentials.EssentialsPlugin;
import de.kalypzo.essentials.command.CommandLoader;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.paper.util.sender.Source;

/**
 * <p>/broadcast </p>
 * <p>Because of @CommandContainer it gets instantiated by {@link CommandLoader}</p>
 */
@CommandContainer
public class BroadcastCommand {

    @Command("broadcast <message>")
    @Permission("essentials.command.broadcast")
    @CommandDescription("Broadcasts a message to the server")
    public void broadcast(Source source, @Greedy String message) {
        EssentialsPlugin.instance().getBroadcastManager().broadcastNetwork(message);
    }
}
