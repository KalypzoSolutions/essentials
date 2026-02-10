package de.kalypzo.essentials.command.admin;

import de.kalypzo.essentials.command.CommandManager;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import de.kalypzo.essentials.EssentialsPlugin;
import de.kalypzo.essentials.user.EssentialsUser;
import de.kalypzo.essentials.util.Text;
import org.bukkit.configuration.file.FileConfiguration;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Flag;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.paper.util.sender.Source;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * <p>/debug </p>
 * <p>Because of @CommandContainer it gets instantiated by {@link CommandManager}</p>
 */
@CommandContainer
public class DebugCommand {

    @Command("debug position <player>")
    @CommandDescription("Gibt die Position eines Spielers aus")
    @Permission("essentials.admin.debug.position")
    public void debugPositon(Source source, EssentialsUser player) {
        long start = System.currentTimeMillis();
        player.getPosition().thenAccept((pos) -> {
            long time = System.currentTimeMillis() - start;
            source.source().sendRichMessage("<gray>» <pos> in <time>",
                    Placeholder.component("pos", pos),
                    Placeholder.unparsed("time", time + "ms"));
        }).exceptionally(ex -> {
            source.source().sendRichMessage("<red>» Fehler beim Laden der Position: " + ex.getMessage());
            return null;
        });
    }

    @Command("debug reload")
    @CommandDescription("reloads the plugin configuration")
    @Permission("essentials.admin.debug.reload")
    public void reloadConfig(Source source, @Flag("printConfig") boolean printConfig) {
        EssentialsPlugin.instance().reloadConfig();
        Text.loadBranding(EssentialsPlugin.instance());
        source.source().sendRichMessage("<#00d492>◆ <#b9f8cf>Configuration reloaded");
        if (printConfig) {
            FileConfiguration config = EssentialsPlugin.instance().getConfig();
            source.source().sendRichMessage("    <gray>=====[ <#00d492>Configuration <gray>]===== ");
            printConfigIndented(config, source, "", 0);
            source.source().sendRichMessage("    <gray>=====[ <#00d492>Configuration <gray>]===== ");
        }
    }

    private void printConfigIndented(FileConfiguration config, Source source, String path, int indent) {
        for (String key : Objects.requireNonNull(config.getConfigurationSection(path.isEmpty() ? "" : path)).getKeys(false)) {
            String fullPath = path.isEmpty() ? key : path + "." + key;
            if (config.isConfigurationSection(fullPath)) {
                String indentStr = " ".repeat(indent * 2);
                source.source().sendRichMessage(indentStr + "<#7bf1a8>" + key + "<dark_gray>:");
                printConfigIndented(config, source, fullPath, indent + 1);
            } else {
                String indentStr = " ".repeat(indent * 2) + " - ";
                Object value = config.get(fullPath);
                source.source().sendRichMessage(indentStr + "<#7bf1a8>" + key + "<dark_gray>: <#ecfdf5>" + value);
            }
        }
    }

    @Command("debug environmentUsers")
    @CommandDescription("Gibt alle Nutzer aus, die dem Environment vorliegen")
    @Permission("essentials.admin.debug.environmentusers")
    public CompletableFuture<Void> printEnvironmentUsers(Source source) {
        return EssentialsPlugin.instance().getEnvironment().getUsers().thenAccept((users) -> {
            source.source().sendRichMessage("<#00d492>◆ <#b9f8cf>Environment Users <gray>(" + users.size() + ")");
            for (EssentialsUser user : users) {
                printUser(source, user);
            }
        }).exceptionally((ex) -> {
            source.source().sendRichMessage("<red>Fehler beim Laden der Nutzer: " + ex.getMessage());
            return null;
        });
    }

    public void printUser(Source source, EssentialsUser user) {
        // Name with UUID as hover, server name after » symbol
        String msg = "<#00d492>» <#ecfdf5><hover:show_text:'<gray>UUID: <#b9f8cf>" + user.getUuid() + "'>" +
                user.getName() + "</hover> <gray>◆ <#b9f8cf>" + user.getServerName();
        source.source().sendRichMessage(msg);
    }

}
