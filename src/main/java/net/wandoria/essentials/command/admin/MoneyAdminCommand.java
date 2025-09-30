package net.wandoria.essentials.command.admin;


import it.einjojo.economy.EconomyService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.translation.Argument;
import net.wandoria.essentials.exception.TransactionException;
import net.wandoria.essentials.user.EssentialsOfflineUser;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.paper.util.sender.Source;

import java.util.concurrent.CompletableFuture;

/**
 * <p>Because of @CommandContainer it gets instantiated by {@link net.wandoria.essentials.command.CommandManager}</p>
 */
@CommandContainer
public class MoneyAdminCommand {
    private final EconomyService economyService;

    public MoneyAdminCommand(EconomyService economyService) {
        this.economyService = economyService;
    }

    @Command("money set <player> <amount>")
    @CommandDescription("Setze den Kontostand eines Spielers")
    @Permission("mirania.central.bukkit.command.money.set")
    public CompletableFuture<Void> setMoney(Source source, EssentialsOfflineUser player, int amount) {
        CommandSender sender = source.source();
        return economyService.setBalance(player.getUniqueId(), amount, "ADMIN_CMD SET " + source.source().getName()).thenAccept((result) -> {
            if (!result.isSuccess()) {
                throw new TransactionException(result.status());
            }
            sender.sendMessage(Component.translatable("essentials.economy.admin.set",
                    Argument.component("target", player),
                    Argument.component("amount", Component.text(amount))
            ));
        });
    }

    @Command("money add <player> <amount>")
    @CommandDescription("Ändere den Kontostand eines Spielers")
    @Permission("mirania.central.bukkit.command.money.add")
    public CompletableFuture<Void> addMoney(Source source, EssentialsOfflineUser player, int amount) {
        CommandSender sender = source.source();
        return economyService.deposit(player.getUniqueId(), amount, "ADMIN_CMD ADD " + source.source().getName()).thenAccept((result) -> {
            if (!result.isSuccess()) {
                throw new TransactionException(result.status());
            }
            sender.sendMessage(Component.translatable("essentials.economy.admin.add",
                    Argument.component("target", player),
                    Argument.numeric("delta", amount),
                    Argument.numeric("amount", result.newBalance().orElse(-1D))
            ));
        });
    }

    @Command("money withdraw <player> <amount>")
    @CommandDescription("Ändere den Kontostand eines Spielers")
    @Permission("mirania.central.bukkit.command.money.add")
    public CompletableFuture<Void> removeMoney(Source source, EssentialsOfflineUser player, int amount) {
        CommandSender sender = source.source();
        return economyService.withdraw(player.getUniqueId(), amount, "ADMIN_CMD SUB " + source.source().getName()).thenAccept((result) -> {
            if (!result.isSuccess()) {
                throw new TransactionException(result.status());
            }
            sender.sendMessage(Component.translatable("essentials.economy.admin.remove",
                    Argument.component("target", player),
                    Argument.numeric("delta", amount),
                    Argument.numeric("amount", result.newBalance().orElse(-1D))
            ));
        });
    }

}
