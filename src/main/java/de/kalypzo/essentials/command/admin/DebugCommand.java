package de.kalypzo.essentials.command.admin;

import de.kalypzo.essentials.EssentialsPlugin;
import de.kalypzo.essentials.user.EssentialsUser;
import de.kalypzo.essentials.util.Text;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import studio.mevera.imperat.BukkitCommandSource;
import studio.mevera.imperat.annotations.types.Description;
import studio.mevera.imperat.annotations.types.Execute;
import studio.mevera.imperat.annotations.types.Permission;
import studio.mevera.imperat.annotations.types.RootCommand;
import studio.mevera.imperat.annotations.types.SubCommand;
import studio.mevera.imperat.annotations.types.Switch;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@RootCommand("debug")
public class DebugCommand {

    @SubCommand("position")
    @Description("Gibt die Position eines Spielers aus")
    @Permission("essentials.admin.debug.position")
    public void debugPositon(BukkitCommandSource source, EssentialsUser player) {
        long start = System.currentTimeMillis();
        player.getPosition().thenAccept((pos) -> {
            long time = System.currentTimeMillis() - start;
            source.origin().sendRichMessage("<gray>» <pos> in <time>",
                    Placeholder.component("pos", pos),
                    Placeholder.unparsed("time", time + "ms"));
        }).exceptionally(ex -> {
            source.origin().sendRichMessage("<red>» Fehler beim Laden der Position: " + ex.getMessage());
            return null;
        });
    }

    @SubCommand("reload")
    @Description("reloads the plugin configuration")
    @Permission("essentials.admin.debug.reload")
    public void reloadConfig(BukkitCommandSource source, @Switch("printConfig") boolean printConfig) {
        EssentialsPlugin.instance().reloadConfig();
        Text.loadBranding(EssentialsPlugin.instance());
        EssentialsPlugin.instance().reloadBroadcasts();
        EssentialsPlugin.instance().reloadHomesConfig();
        EssentialsPlugin.instance().reloadWarpsConfig();
        source.origin().sendRichMessage("<#00d492>◆ <#b9f8cf>Configuration reloaded");
        if (printConfig) {
            FileConfiguration config = EssentialsPlugin.instance().getConfig();
            source.origin().sendRichMessage("    <gray>=====[ <#00d492>Configuration <gray>]===== ");
            printConfigIndented(config, source, "", 0);
            source.origin().sendRichMessage("    <gray>=====[ <#00d492>Configuration <gray>]===== ");
        }
    }

    private void printConfigIndented(FileConfiguration config, BukkitCommandSource source, String path, int indent) {
        for (String key : Objects.requireNonNull(config.getConfigurationSection(path.isEmpty() ? "" : path)).getKeys(false)) {
            String fullPath = path.isEmpty() ? key : path + "." + key;
            if (config.isConfigurationSection(fullPath)) {
                String indentStr = " ".repeat(indent * 2);
                source.origin().sendRichMessage(indentStr + "<#7bf1a8>" + key + "<dark_gray>:");
                printConfigIndented(config, source, fullPath, indent + 1);
            } else {
                String indentStr = " ".repeat(indent * 2) + " - ";
                Object value = config.get(fullPath);
                source.origin().sendRichMessage(indentStr + "<#7bf1a8>" + key + "<dark_gray>: <#ecfdf5>" + value);
            }
        }
    }

    @SubCommand("environmentUsers")
    public static class EnvironmentUsersCommand {

        @Execute
        @Description("Gibt alle Nutzer aus, die dem Environment vorliegen")
        @Permission("essentials.admin.debug.environmentusers")
        public CompletableFuture<Void> printEnvironmentUsers(BukkitCommandSource source) {
            return EssentialsPlugin.instance().getEnvironment().getUsers().thenAccept((users) -> {
                source.origin().sendRichMessage("<#00d492>◆ <#b9f8cf>Environment Users <gray>(" + users.size() + ")");
                for (EssentialsUser user : users) {
                    printUser(source, user);
                }
            }).exceptionally((ex) -> {
                source.origin().sendRichMessage("<red>Fehler beim Laden der Nutzer: " + ex.getMessage());
                return null;
            });
        }

        @SubCommand("delete")
        @Description("Löscht die Environment-Connection eines Spielers")
        @Permission("essentials.admin.debug.environmentusers.delete")
        public CompletableFuture<Void> deleteEnvironmentUser(BukkitCommandSource source, EssentialsUser player) {
            Player localPlayer = Bukkit.getPlayer(player.getUuid());
            if (localPlayer != null) {
                Bukkit.getScheduler().runTask(EssentialsPlugin.instance(), () ->
                        localPlayer.kick(Text.deserialize("<red>Deine PlayerAPI-Connection wurde neu geladen.")));
            }
            return EssentialsPlugin.instance().getEnvironment().deletePlayerConnection(player.getUuid()).thenAccept((deleted) -> {
                if (deleted) {
                    source.origin().sendRichMessage("<#00d492>◆ <#b9f8cf>Connection gelöscht: <#ecfdf5>" + player.getName());
                } else {
                    source.origin().sendRichMessage("<red>◆ Connection konnte nicht gelöscht werden: " + player.getName());
                }
            }).exceptionally((ex) -> {
                source.origin().sendRichMessage("<red>Fehler beim Löschen der Connection: " + ex.getMessage());
                return null;
            });
        }

        @SubCommand("refresh")
        @Description("Aktualisiert Environment-User und löscht Ghost-Spieler dieses Servers")
        @Permission("essentials.admin.debug.environmentusers.refresh")
        public CompletableFuture<Void> refreshEnvironmentUsers(BukkitCommandSource source) {
            return EssentialsPlugin.instance().getEnvironment().refreshPlayerConnections().thenAccept((result) ->
                    source.origin().sendRichMessage("<#00d492>◆ <#b9f8cf>Environment aktualisiert <gray>(geprueft: " +
                            result.checked() + ", gelöscht: " + result.deleted() + ", fehlgeschlagen: " + result.failed() + ")")
            ).exceptionally((ex) -> {
                source.origin().sendRichMessage("<red>Fehler beim Aktualisieren der Connections: " + ex.getMessage());
                return null;
            });
        }
    }

    public static void printUser(BukkitCommandSource source, EssentialsUser user) {
        String msg = "<#00d492>» <#ecfdf5><hover:show_text:'<gray>UUID: <#b9f8cf>" + user.getUuid() + "'>" +
                user.getName() + "</hover> <gray>◆ <#b9f8cf>" + user.getServerName();
        source.origin().sendRichMessage(msg);
    }
}
