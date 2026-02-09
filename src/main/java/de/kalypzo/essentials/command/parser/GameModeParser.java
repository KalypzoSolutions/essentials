package de.kalypzo.essentials.command.parser;

import de.kalypzo.essentials.exception.ComponentException;
import org.bukkit.GameMode;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.paper.util.sender.Source;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;

import java.util.List;

public class GameModeParser implements ArgumentParser<Source, GameMode>, BlockingSuggestionProvider.Strings<Source> {


    public static ParserDescriptor<Source, GameMode> descriptor() {
        return ParserDescriptor.parserDescriptor(new GameModeParser(), GameMode.class);
    }
    @Override
    public @NonNull ArgumentParseResult<@NonNull GameMode> parse(@NonNull CommandContext<@NonNull Source> commandContext, @NonNull CommandInput commandInput) {
        String input = commandInput.readString();
        switch (input) {
            case "1", "c", "creative" -> {
                return ArgumentParseResult.success(GameMode.CREATIVE);
            }
            case "2", "a", "adventure" -> {
                return ArgumentParseResult.success(GameMode.ADVENTURE);
            }
            case "3", "o", "spectator" -> {
                return ArgumentParseResult.success(GameMode.SPECTATOR);
            }
            case "0", "s", "survival" -> {
                return ArgumentParseResult.success(GameMode.SURVIVAL);
            }
            default -> {
                return ArgumentParseResult.failure(ComponentException.translatable("essentials.gamemode.invalid", input));
            }
        }
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(@NonNull CommandContext<Source> commandContext, @NonNull CommandInput input) {
        return List.of("1", "2", "3", "0", "survival", "creative", "adventure", "spectator");
    }
}
