package net.wandoria.essentials.command.chat;

import net.wandoria.essentials.EssentialsPlugin;
import net.wandoria.essentials.util.Text;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.paper.util.sender.Source;

/**
 * <p>Provides all commands related to private messages / reply </p>
 * <p>Because of @CommandContainer it gets instantiated by {@link net.wandoria.essentials.command.CommandManager}</p>
 */
@CommandContainer
public class InfoCommand {
    private final EssentialsPlugin plugin;

    public InfoCommand() {
        this.plugin = EssentialsPlugin.instance();
    }

    @Command("info")
    public void info(Source source) {
        plugin.getConfig().getString("info-message", "info-message in config.yml not set.").lines().forEach(line -> {
            source.source().sendMessage(Text.deserialize(line));
        });
    }

}
