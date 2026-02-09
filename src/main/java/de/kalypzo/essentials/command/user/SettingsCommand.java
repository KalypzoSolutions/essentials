package de.kalypzo.essentials.command.user;

import de.kalypzo.essentials.command.CommandManager;
import net.kyori.adventure.text.Component;
import de.kalypzo.essentials.EssentialsPlugin;
import de.kalypzo.essentials.user.UserSettings;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.paper.util.sender.PlayerSource;

import java.util.concurrent.CompletableFuture;

/**
 *
 * <p>Because of @CommandContainer it gets instantiated by {@link CommandManager}</p>
 */
@CommandContainer
public class SettingsCommand {

    @Command("settings togglePing")
    @Command("einstellungen togglePing")
    @Command("togglePing")
    public CompletableFuture<Void> togglePingSounds(PlayerSource source) {
        var settings = getSettings(source);
        boolean isDisabled = settings.disabledPingSound();
        return settings.disabledPingSound(!isDisabled).thenRun(() -> {
            if (isDisabled) { //

                source.source().sendMessage(Component.translatable("essentials.settings.ping-active"));
                EssentialsPlugin.instance().getChatSystem().playPingSound(source.source());
            } else {
                source.source().sendMessage(Component.translatable("essentials.settings.ping-disabled"));
            }
        });



    }

    private static UserSettings getSettings(PlayerSource source) {
        return UserSettings.of(source.source().getUniqueId());
    }

}
