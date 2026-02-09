package de.kalypzo.essentials.command.parser;

import de.kalypzo.essentials.exception.ComponentException;
import de.kalypzo.essentials.world.warps.Warp;
import de.kalypzo.essentials.world.warps.WarpManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.paper.util.sender.Source;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class WarpParser implements ArgumentParser<Source, Warp>, BlockingSuggestionProvider.Strings<Source> {

    public static ParserDescriptor<Source, Warp> descriptor() {
        return ParserDescriptor.of(new WarpParser(), Warp.class);
    }

    @Override
    public ArgumentParseResult<Warp> parse(CommandContext<Source> commandContext, CommandInput commandInput) {
        final String input = commandInput.readString();
        var optWarp = WarpManager.getInstance().getWarp(input);
        return optWarp.map(ArgumentParseResult::success).orElseGet(() -> ArgumentParseResult.failure(
                ComponentException.translatable("essentials.warp.not-found", input)));
    }


    @Override
    public Iterable<String> stringSuggestions(CommandContext<Source> commandContext, CommandInput input) {
        return WarpManager.getInstance().getWarpNames();
    }


}
