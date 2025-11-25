package net.wandoria.essentials.command.user;

import net.kyori.adventure.text.Component;
import net.wandoria.essentials.EssentialsPlugin;
import net.wandoria.essentials.user.UserSettings;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.paper.util.sender.PlayerSource;

/**
 *
 * <p>Because of @CommandContainer it gets instantiated by {@link net.wandoria.essentials.command.CommandManager}</p>
 */
@CommandContainer
public class SettingsCommand {

    @Command("settings togglePing")
    @Command("einstellungen togglePing")
    @Command("togglePing")
    public void togglePingSounds(PlayerSource source) {
        var settings = getSettings(source);
        boolean disabled = settings.disabledPingSound();
        settings.disabledPingSound(!disabled);

        if (!disabled) {
            source.source().sendMessage(Component.translatable("essentials.settings.ping-active"));
            EssentialsPlugin.instance().getChatSystem().playPingSound(source.source());
        } else {
            source.source().sendMessage(Component.translatable("essentials.settings.ping-disabled"));
        }

    }

    private static UserSettings getSettings(PlayerSource source) {
        return UserSettings.of(source.source().getUniqueId());
    }

}
