package net.wandoria.essentials.command.parser;


import net.wandoria.essentials.exception.ComponentException;
import net.wandoria.essentials.user.home.Home;
import net.wandoria.essentials.user.home.HomeManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.paper.util.sender.Source;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@NullMarked
public class HomeParser implements ArgumentParser.FutureArgumentParser<Source, Home>, SuggestionProvider<Source> {
    public static ParserDescriptor<Source, Home> descriptor() {
        return ParserDescriptor.of(new HomeParser(), Home.class);
    }

    @Override
    public CompletableFuture<ArgumentParseResult<Home>> parseFuture(CommandContext<Source> commandContext, CommandInput commandInput) {
        final String input = commandInput.readString();
        if (commandContext.sender().source() instanceof Player playerSender) {
            return HomeManager.getInstance().getHome(playerSender.getUniqueId(), input).thenApply(optHome ->
                    optHome.map(ArgumentParseResult::success)
                            .orElseGet(() -> ArgumentParseResult.failure(
                                    ComponentException.translatable("essentials.home.not-found", input)
                            ))
            );

        }
        return CompletableFuture.completedFuture(ArgumentParseResult.failure(
                ComponentException.translatable("essentials.command.player-only")));
    }


    @Override
    public CompletableFuture<? extends Iterable<? extends Suggestion>> suggestionsFuture(CommandContext<Source> context, CommandInput input) {
        CommandSender sender = context.sender().source();
        if (sender instanceof Player player) {
            return HomeManager.getInstance().getHomes(player.getUniqueId()).thenApply((homes -> {
                List<Suggestion> suggestions = new ArrayList<>();
                for (Home h : homes) {
                    suggestions.add(Suggestion.suggestion(h.name()));
                }
                return suggestions;
            }));
        }
        return CompletableFuture.completedFuture(Collections.emptyList());
    }


}
