package net.wandoria.essentials.command.admin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.labymod.serverapi.server.bukkit.LabyModProtocolService;
import net.wandoria.essentials.util.Text;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.paper.util.sender.Source;

public class LabyModDebugCommand {

    @Command("essentials labymod players")
    @CommandDescription("Lists all players with LabyMod installed")
    @Permission("essentials.admin.labymod.players")
    public void listLabyModPlayers(Source source) {
        var labyPlayers = LabyModProtocolService.get().getPlayers();

        if (labyPlayers.isEmpty()) {
            source.source().sendMessage(Text.deserialize("<prefix> <p>Keine Labymod Spieler online.</p>"));
            return;
        }
        source.source().sendMessage(Text.deserialize("<prefix> <p>LabyMod Players:</p>"));
        for (var player : labyPlayers) {
            player.requestInstalledAddons(addonsResp -> {
                source.source().sendMessage(Text.deserialize("<gray> - <hl><name></hl> <dark_gray>(Version: <hl><version></hl>)</dark_gray>",
                        Placeholder.unparsed("version", player.getLabyModVersion()),
                        Placeholder.unparsed("name", player.getPlayer().getName())));
                var addons = addonsResp.getInstalledAddons();
                if (addons.isEmpty()) {
                    source.source().sendMessage(Text.deserialize("     <gray>Keine Addons."));
                    return;
                }
                source.source().sendMessage(Text.deserialize("     <h2>Addons:"));
                for (var addon : addons) {
                    Component disableButton = addon.isEnabled() ? Component.text("[X]", Text.ERROR_COLOR)
                            .clickEvent(ClickEvent.runCommand("/essentials labymod disableaddon " + player.getPlayer().getName() + " " + addon.getNamespace()))
                            : Component.empty();
                    source.source().sendMessage(Text.deserialize("     <gray>- <active> <hl><namespace></hl> <dark_gray>(Version: <hl><version></hl>)</dark_gray> <button>",
                            Placeholder.component("button", disableButton),
                            Placeholder.parsed("active", addon.isEnabled() ? "<green>●" : "<red>●"),
                            Placeholder.unparsed("namespace", addon.getNamespace()),
                            Placeholder.unparsed("version", addon.getVersion().toString())
                    ));
                }
            });

        }
    }


    @Command("essentials labymod disableaddon <player> <addon>")
    @CommandDescription("Disables a LabyMod addon for a specific player")
    @Permission("essentials.admin.labymod.disableaddon")
    public void disableLabyModAddon(Source source, Player player, String addon) {
        var labyPlayer = LabyModProtocolService.get().getPlayer(player.getUniqueId());
        if (labyPlayer == null) {
            source.source().sendMessage(Text.deserialize("<prefix> <red>Der Spieler <hl>" + player.getName() + "</hl> hat LabyMod nicht installiert."));
            return;
        }
        labyPlayer.disableAddons(addon);
        source.source().sendMessage(Text.deserialize("<prefix> <green>Das Addon <hl>" + addon + "</hl> wurde für Spieler <hl>" + player.getName() + "</hl> deaktiviert."));
    }


}
