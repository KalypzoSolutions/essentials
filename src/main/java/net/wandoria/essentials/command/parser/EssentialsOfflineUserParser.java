package net.wandoria.essentials.command.parser;


import net.wandoria.essentials.environment.PluginEnvironment;
import net.wandoria.essentials.exception.ComponentException;
import net.wandoria.essentials.user.EssentialsOfflineUser;
import net.wandoria.essentials.user.EssentialsUser;
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
 * Injects an {@link net.wandoria.essentials.user.EssentialsOfflineUser} based on the provided input.
 * <ul>
 *     <li>UUID (when length 36)</li>
 *     <li>Username</li>
 * </ul>
 */
@NullMarked
public class EssentialsOfflineUserParser implements ArgumentParser.FutureArgumentParser<Source, EssentialsOfflineUser>,
        SuggestionProvider<Source> {
    private static final int UUID_LENGTH = 36;
    private final UUIDParser<Source> uuidParser = new UUIDParser<>();
    private final PluginEnvironment environment;

    public EssentialsOfflineUserParser(PluginEnvironment environment) {
        this.environment = environment;
    }

    public static ParserDescriptor<Source, EssentialsOfflineUser> descriptor(PluginEnvironment environment) {
        return ParserDescriptor.parserDescriptor(new EssentialsOfflineUserParser(environment),
                EssentialsOfflineUser.class);
    }


    @Override
    public CompletableFuture<ArgumentParseResult<EssentialsOfflineUser>> parseFuture(
            CommandContext<Source> commandContext,
            CommandInput commandInput) {
        String input = commandInput.peekString();
        final CompletableFuture<Optional<EssentialsOfflineUser>> futureNetworkUser;
        if (input.length() == UUID_LENGTH) {
            // Wir nutzen bereits existierende Parser für bessere Fehlermeldungen
            ArgumentParseResult<UUID> parsedUUID = uuidParser.parse(commandContext, commandInput.copy());
            if (parsedUUID.failure().isPresent()) {
                return CompletableFuture.completedFuture(ArgumentParseResult.failure(parsedUUID.failure().get()));
            }
            futureNetworkUser = environment.getOfflineUser(parsedUUID.parsedValue().orElseThrow());
        } else {
            futureNetworkUser = environment.getOfflineUserByName(commandInput.peekString());
        }

        return futureNetworkUser.thenApply(optionalNetworkUser -> {
            if (optionalNetworkUser.isEmpty()) {
                return ArgumentParseResult.failure(ComponentException.translatable("wandoria.player.not-found", input));
            }
            commandInput.readString();
            return ArgumentParseResult.success(optionalNetworkUser.get());
        });
    }


    @Override
    public CompletableFuture<? extends Iterable<? extends Suggestion>> suggestionsFuture(CommandContext<Source> context, CommandInput input) {
        return environment.getUsers().thenApply(
                users -> users.stream()
                        .map(EssentialsUser::getName)
                        .map(Suggestion::suggestion)
                        .toList()
        );
    }
}
