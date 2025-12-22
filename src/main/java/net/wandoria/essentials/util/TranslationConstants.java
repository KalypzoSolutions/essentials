package net.wandoria.essentials.util;


import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;

@UtilityClass
public class TranslationConstants {

    public static final Component GENERIC_ERROR = Component.translatable("wandoria.generic.error");
    public static final Component PLAYER_NOT_FOUND = Component.translatable("essentials.player.not-found");
    public static final Component PLAYER_OFFLINE = Component.translatable("wandoria.player.is-offline");
    public static final Component COMMAND_REQUIRES_PLAYER_EXECUTOR = Component.translatable("wandoria.command.requires-player-executor");

    public static Component createReferencedError(String reference) {
        return Component.translatable("wandoria.generic.error-referenced", Component.text(reference));
    }

}
