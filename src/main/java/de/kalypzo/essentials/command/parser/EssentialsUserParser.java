package de.kalypzo.essentials.command.parser;


import it.einjojo.playerapi.PlayerApiProvider;
import de.kalypzo.essentials.environment.PluginEnvironment;
import de.kalypzo.essentials.exception.ComponentException;
import de.kalypzo.essentials.user.EssentialsUser;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.paper.util.sender.Source;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.parser.standard.UUIDParser;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;
import org.jspecify.annotations.NullMarked;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


/**
 * Injects an {@link EssentialsUser} based on the provided input.
 * <ul>
 *     <li>UUID (when length 36)</li>
 *     <li>Username</li>
 * </ul>
 */
@NullMarked
public class EssentialsUserParser implements ArgumentParser.FutureArgumentParser<Source, EssentialsUser>, SuggestionProvider<Source> {
    private static final int UUID_LENGTH = 36;
    private final UUIDParser<Source> uuidParser = new UUIDParser<>();
    private final PluginEnvironment environment;

    public EssentialsUserParser(PluginEnvironment environment) {
        this.environment = environment;
    }

    public static ParserDescriptor<Source, EssentialsUser> descriptor(PluginEnvironment environment) {
        return ParserDescriptor.parserDescriptor(new EssentialsUserParser(environment), EssentialsUser.class);
    }


    @Override
    public CompletableFuture<ArgumentParseResult<EssentialsUser>> parseFuture(CommandContext<Source> commandContext, CommandInput commandInput) {
        String input = commandInput.peekString();
        final CompletableFuture<Optional<EssentialsUser>> futureNetworkUser;

        if (input.length() == UUID_LENGTH) {
            // für bessere Fehlermeldungen nutzen wir den bereits existierenden UUIDParser
            ArgumentParseResult<UUID> parsedUUID = uuidParser.parse(commandContext, commandInput.copy());
            if (parsedUUID.failure().isPresent()) {
                return CompletableFuture.completedFuture(ArgumentParseResult.failure(parsedUUID.failure().get()));
            }
            futureNetworkUser = environment.getUser(parsedUUID.parsedValue().orElseThrow());
        } else {
            futureNetworkUser = environment.getUserByName(commandInput.peekString());
        }
        return futureNetworkUser.thenApply(optionalNetworkUser -> {
            if (optionalNetworkUser.isEmpty()) {
                return ArgumentParseResult.failure(ComponentException.translatable("essentials.player.offline", input));
            }
            commandInput.readString();
            return ArgumentParseResult.success(optionalNetworkUser.get());
        });
    }


    @Override
    public CompletableFuture<? extends Iterable<? extends Suggestion>> suggestionsFuture(CommandContext<Source> context, CommandInput input) {
        return PlayerApiProvider.getInstance().getOnlinePlayerNames().thenApply(strings -> strings.stream().map(Suggestion::suggestion).toList()); //
    }
}
