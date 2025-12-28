package net.wandoria.essentials.command.parser;

import net.wandoria.essentials.exception.ComponentException;
import net.wandoria.essentials.user.WildcardEssentialUser;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.paper.util.sender.Source;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;

public class MultiUserParser implements ArgumentParser<Source, WildcardEssentialUser>, BlockingSuggestionProvider.Strings<Source> {

    public static ParserDescriptor<Source, WildcardEssentialUser> descriptor() {
        return ParserDescriptor.of(new MultiUserParser(), WildcardEssentialUser.class);
    }

    @Override
    public @NonNull ArgumentParseResult<WildcardEssentialUser> parse(@NonNull CommandContext<Source> commandContext, @NonNull CommandInput commandInput) {
        String input = commandInput.peekString();
        if (input.equals("*")) {
            return ArgumentParseResult.success(WildcardEssentialUser.INSTANCE);
        } else {
            return ArgumentParseResult.failure(ComponentException.translatable("essentials.player.offline", input));
        }
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(@NonNull CommandContext<Source> commandContext, @NonNull CommandInput input) {
        return java.util.List.of("*");
    }
}
