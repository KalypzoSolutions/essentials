package net.wandoria.essentials.command.user;


import it.einjojo.economy.EconomyService;
import it.einjojo.economy.db.AccountData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.translation.Argument;
import net.wandoria.essentials.EssentialsPlugin;
import net.wandoria.essentials.environment.PluginEnvironment;
import net.wandoria.essentials.exception.ComponentException;
import net.wandoria.essentials.exception.TransactionException;
import net.wandoria.essentials.user.EssentialsOfflineUser;
import net.wandoria.essentials.user.leaderboard.BalanceTopPostgresAccessor;
import net.wandoria.essentials.util.NumberFormatter;
import net.wandoria.essentials.util.TranslationConstants;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.paper.util.sender.PlayerSource;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * <p>Provides all commands related to user money </p>
 * <p>Because of @CommandContainer it gets instantiated by {@link net.wandoria.essentials.command.CommandManager}</p>
 */
@CommandContainer
public class MoneyCommand {
    private final PluginEnvironment environment;
    private final EconomyService economyService;
    private final BalanceTopPostgresAccessor balanceTopAccessor;

    public MoneyCommand() {
        this.environment = EssentialsPlugin.environment();
        this.economyService = Objects.requireNonNull(Bukkit.getServicesManager().load(EconomyService.class), "EconomyService not found in Bukkit Service Registry");
        this.balanceTopAccessor = new BalanceTopPostgresAccessor(EssentialsPlugin.instance().getDataSource(), "economy_balances");
    }

    @Command("money|coins|balance")
    @CommandDescription("Zeigt deinen Kontostand an")
    public CompletableFuture<Void> showMoney(PlayerSource sender) {
        UUID player = sender.source().getUniqueId();
        return economyService.getBalance(player).thenAccept((balance) -> {
            sender.source().sendMessage(Component.translatable("essentials.money.balance.own",
                    Argument.component("amount", Component.text(NumberFormatter.doubleToHumanReadable(balance)))
            ));
        });
    }


    @Command("pay <player> <amount>")
    @CommandDescription("Überweise Geld an einen Spieler")
    public CompletableFuture<Void> pay(PlayerSource sender, EssentialsOfflineUser player, int amount) {
        if (amount < 1) {
            throw ComponentException.translatable("essentials.money.pay.amount-too-low");
        }
        if (sender.source().getUniqueId().equals(player.getUniqueId())) {
            throw ComponentException.translatable("essentials.money.pay.no-self-pay");
        }
        return economyService.withdraw(sender.source().getUniqueId(), amount, "pay to " + player.getName())
                .thenCompose((result) -> {
                    if (!result.isSuccess()) {
                        throw new TransactionException(result.status());
                    }
                    return economyService.deposit(player.getUniqueId(), amount, "pay from " + sender.source().getName())
                            .thenAccept((success) -> {
                                sender.source().sendMessage(Component.translatable("essentials.money.pay.sent",
                                        Argument.component("target", Component.text(player.getName())),
                                        Argument.component("amount", Component.text(NumberFormatter.doubleToHumanReadable(amount)))
                                ));
                                notifyPaymentReceive(player.getUniqueId(), sender.source().displayName(), amount);
                            });
                });
    }

    /**
     * If the target user is online, notify him that he received money
     *
     * @param target UUID of the target user
     * @param sender Component of the sender
     * @param amount Amount of money received
     */
    private void notifyPaymentReceive(UUID target, Component sender, int amount) {
        environment.getUser(target).thenAccept((optional) -> {
            optional.ifPresent((user) -> {
                user.sendMessage(Component.translatable("essentials.money.pay.received",
                        Argument.component("sender", sender),
                        Argument.component("amount", Component.text(NumberFormatter.doubleToHumanReadable(amount)))
                ));
            });
        });

    }

    @Command("money|coins|balance top")
    @CommandDescription("Zeige die Top 10 reichsten Spieler an.")
    public void showEco(PlayerSource sender) {
        boolean canRefresh = balanceTopAccessor.getLastUpdate().plusMillis(1000 * 30).isBefore(Instant.now());
        if (canRefresh) {
            balanceTopAccessor.refreshTopTenAsync();
        }

        AccountData[] topTen = balanceTopAccessor.getTopTen();
        sender.source().sendRichMessage("<gray>Die 10 reichsten Spieler sind");
        for (int i = 0; i < topTen.length; i++) {
            var data = topTen[i];
            String money = (data == null ? "?" : NumberFormatter.doubleToHumanReadable(data.balance()));
            String name = (data == null ? "?" : Bukkit.getOfflinePlayer(data.uuid()).getName());
            sender.source().sendRichMessage("<dark_gray>◆ <gray><pos> <#b9f8cf><name> <yellow><money> ",
                    Placeholder.unparsed("pos", i + 1 + ""),
                    Placeholder.unparsed("money", money),
                    Placeholder.unparsed("name", name == null ? "unknown" : name)
            );
        }
    }

}
