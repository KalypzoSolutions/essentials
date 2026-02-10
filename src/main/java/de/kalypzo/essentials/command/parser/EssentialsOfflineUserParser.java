package de.kalypzo.essentials.command.parser;


import com.mojang.brigadier.LiteralMessage;
import de.kalypzo.essentials.environment.PluginEnvironment;
import de.kalypzo.essentials.exception.ComponentException;
import de.kalypzo.essentials.user.EssentialsOfflineUser;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.brigadier.suggestion.TooltipSuggestion;
import org.incendo.cloud.bukkit.BukkitCommandContextKeys;
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
 * Injects an {@link EssentialsOfflineUser} based on the provided input.
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
        final CompletableFuture<Optional<EssentialsOfflineUser>> futureOfflineUser;
        if (input.length() == UUID_LENGTH) {
            // Wir nutzen bereits existierende Parser für bessere Fehlermeldungen
            ArgumentParseResult<UUID> parsedUUID = uuidParser.parse(commandContext, commandInput.copy());
            if (parsedUUID.failure().isPresent()) {
                return CompletableFuture.completedFuture(ArgumentParseResult.failure(parsedUUID.failure().get()));
            }
            futureOfflineUser = environment.getOfflineUser(parsedUUID.parsedValue().orElseThrow());
        } else {
            futureOfflineUser = environment.getOfflineUserByName(commandInput.peekString());
        }
        commandInput.readString();

        return futureOfflineUser.thenApply(optionalNetworkUser -> {
            if (optionalNetworkUser.isEmpty()) {
                return ArgumentParseResult.failure(ComponentException.translatable("essentials.player.not-found", input));
            }
            return ArgumentParseResult.success(optionalNetworkUser.get());
        });
    }


    @Override
    public CompletableFuture<? extends Iterable<? extends Suggestion>> suggestionsFuture(CommandContext<Source> context, CommandInput input) {
        CommandSender sender = context.get(BukkitCommandContextKeys.BUKKIT_COMMAND_SENDER);
        if (sender instanceof Player player) {
            return environment.suggestOfflinePlayerNames(input.peekString(), player.getUniqueId()).thenApply(list -> list.stream().map(s -> {
                return TooltipSuggestion.suggestion(s, new LiteralMessage("tooltip: " + s));
            }).toList());
        }
        return environment.suggestOfflinePlayerNames(input.peekString(), null).thenApply(list -> list.stream().map(Suggestion::suggestion).toList());
    }
}
