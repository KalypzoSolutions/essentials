package de.kalypzo.essentials.command.user;

import de.kalypzo.essentials.EssentialsPlugin;
import de.kalypzo.essentials.environment.PluginEnvironment;
import de.kalypzo.essentials.exception.TransactionException;
import de.kalypzo.essentials.user.EssentialsOfflineUser;
import de.kalypzo.essentials.user.leaderboard.BalanceTopPostgresAccessor;
import de.kalypzo.essentials.util.NumberFormatter;
import de.kalypzo.essentials.util.Text;
import it.einjojo.economy.EconomyService;
import it.einjojo.economy.db.AccountData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.translation.Argument;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import studio.mevera.imperat.BukkitCommandSource;
import studio.mevera.imperat.annotations.types.Description;
import studio.mevera.imperat.annotations.types.Execute;
import studio.mevera.imperat.annotations.types.Optional;
import studio.mevera.imperat.annotations.types.Permission;
import studio.mevera.imperat.annotations.types.RootCommand;
import studio.mevera.imperat.annotations.types.SubCommand;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RootCommand({"money", "coins", "balance"})
public class BalanceCommand {
    private final PluginEnvironment environment;
    private final EconomyService economyService;
    private final BalanceTopPostgresAccessor balanceTopAccessor;

    public BalanceCommand() {
        this.environment = EssentialsPlugin.environment();
        this.economyService = Objects.requireNonNull(Bukkit.getServicesManager().load(EconomyService.class), "EconomyService not found in Bukkit Service Registry");
        this.balanceTopAccessor = new BalanceTopPostgresAccessor(EssentialsPlugin.instance().getDataSource(), "economy_balances");
    }

    @Execute
    @Description("Zeigt deinen Kontostand an")
    public CompletableFuture<Void> showMoney(Player sender) {
        UUID player = sender.getUniqueId();
        return economyService.getBalance(player).thenAccept((balance) -> {
            sender.sendMessage(Component.translatable("essentials.money.balance.own",
                    Argument.numeric("amount", balance)
            ));
        });
    }

    @Execute
    @Description("Zeigt den Kontostand eines anderen Spielers an")
    @Permission("essentials.command.money.others")
    public CompletableFuture<Void> showOtherMoney(Player sender, EssentialsOfflineUser player) {
        return economyService.getBalance(player.getUniqueId()).thenAccept((balance) -> {
            sender.sendMessage(Component.translatable("essentials.money.balance.other",
                    Argument.component("target", Component.text(player.getName())),
                    Argument.numeric("amount", balance)
            ));
        });
    }

    @SubCommand("top")
    @Description("Zeige die Top 10 reichsten Spieler an.")
    public CompletableFuture<Void> showEco(Player sender) {
        refreshBalanceStatsIfNeeded();
        if (balanceTopAccessor.getUpdateFuture() == null || balanceTopAccessor.getUpdateFuture().isDone()) {
            return showBaltop(sender);
        } else {
            return balanceTopAccessor.getUpdateFuture().thenCompose(v -> showBaltop(sender));
        }
    }

    @SubCommand("total")
    @Description("Zeigt das gesamte Geld aller Spieler an.")
    public CompletableFuture<Void> showTotal(BukkitCommandSource source) {
        refreshBalanceStatsIfNeeded();
        if (balanceTopAccessor.getUpdateFuture() == null || balanceTopAccessor.getUpdateFuture().isDone()) {
            return showBalanceTotal(source);
        } else {
            return balanceTopAccessor.getUpdateFuture().thenCompose(v -> showBalanceTotal(source));
        }
    }

    @SubCommand("set")
    @Description("Setze den Kontostand eines Spielers")
    @Permission("essentials.admin.money.set")
    public CompletableFuture<Void> setMoney(BukkitCommandSource source, EssentialsOfflineUser player, int amount) {
        return economyService.setBalance(player.getUniqueId(), amount, "ADMIN_CMD SET " + source.name()).thenAccept((result) -> {
            if (!result.isSuccess()) {
                throw new TransactionException(result.status());
            }
            source.origin().sendMessage(Component.translatable("essentials.economy.admin.set",
                    Argument.component("target", player),
                    Argument.numeric("amount", amount)
            ));
        });
    }

    @SubCommand("add")
    @Description("Ändere den Kontostand eines Spielers")
    @Permission("essentials.admin.money.add")
    public CompletableFuture<Void> addMoney(BukkitCommandSource source, EssentialsOfflineUser player, int amount) {
        return economyService.deposit(player.getUniqueId(), amount, "ADMIN_CMD ADD " + source.name()).thenAccept((result) -> {
            if (!result.isSuccess()) {
                throw new TransactionException(result.status());
            }
            source.origin().sendMessage(Component.translatable("essentials.economy.admin.add",
                    Argument.component("target", player),
                    Argument.numeric("delta", amount),
                    Argument.numeric("amount", result.newBalance().orElse(-1D))
            ));
        });
    }

    @SubCommand("withdraw")
    @Description("Ändere den Kontostand eines Spielers")
    @Permission("essentials.admin.money.add")
    public CompletableFuture<Void> removeMoney(BukkitCommandSource source, EssentialsOfflineUser player, int amount) {
        return economyService.withdraw(player.getUniqueId(), amount, "ADMIN_CMD SUB " + source.name()).thenAccept((result) -> {
            if (!result.isSuccess()) {
                throw new TransactionException(result.status());
            }
            source.origin().sendMessage(Component.translatable("essentials.economy.admin.remove",
                    Argument.component("target", player),
                    Argument.numeric("delta", amount),
                    Argument.numeric("amount", result.newBalance().orElse(-1D))
            ));
        });
    }

    private CompletableFuture<Void> showBaltop(BukkitCommandSource sender) {
        return showBaltopForSender(sender.origin());
    }

    private CompletableFuture<Void> showBaltop(Player sender) {
        return showBaltopForSender(sender);
    }

    private CompletableFuture<Void> showBaltopForSender(org.bukkit.command.CommandSender sender) {
        AccountData[] topTen = balanceTopAccessor.getTopTen();
        sender.sendRichMessage("<gray>Die 10 reichsten Spieler sind");

        @SuppressWarnings("unchecked")
        CompletableFuture<String[]>[] lineFutures = new CompletableFuture[topTen.length];

        for (int i = 0; i < topTen.length; i++) {
            final int pos = i;
            AccountData data = topTen[i];

            if (data == null) {
                lineFutures[pos] = CompletableFuture.completedFuture(
                        new String[]{String.valueOf(pos + 1), "-", "-"}
                );
                continue;
            }

            lineFutures[pos] = EssentialsPlugin.environment()
                    .getOfflineUser(data.uuid())
                    .thenApply(optUser -> {
                        String name = optUser
                                .map(EssentialsOfflineUser::getName)
                                .orElse(data.uuid().toString());
                        String money = NumberFormatter.doubleToHumanReadable(data.balance());
                        return new String[]{String.valueOf(pos + 1), name, money};
                    });
        }

        return CompletableFuture.allOf(lineFutures)
                .thenRun(() -> {
                    for (CompletableFuture<String[]> future : lineFutures) {
                        String[] line = future.join();
                        sender.sendRichMessage(
                                "<dark_gray>◆ <gray><pos> <#b9f8cf><name> <yellow><money>",
                                Placeholder.unparsed("pos", line[0]),
                                Placeholder.unparsed("name", line[1]),
                                Placeholder.unparsed("money", line[2])
                        );
                    }
                });
    }

    private void refreshBalanceStatsIfNeeded() {
        boolean canRefresh = balanceTopAccessor.getLastUpdate().plusMillis(1000 * 30).isBefore(Instant.now());
        if (canRefresh) {
            balanceTopAccessor.refreshTopTenAsync();
        }
    }

    private CompletableFuture<Void> showBalanceTotal(BukkitCommandSource source) {
        String total = NumberFormatter.doubleToHumanReadable(balanceTopAccessor.getTotalBalance());
        source.origin().sendMessage(Text.deserialize("<prefix> <p>Alle Spieler besitzen zusammen <hl><total></hl> Coins.",
                Placeholder.unparsed("total", total)));
        return CompletableFuture.completedFuture(null);
    }

    private void notifyPaymentReceive(UUID target, Component sender, int amount) {
        environment.getUser(target).thenAccept((optional) -> {
            optional.ifPresent((user) -> {
                user.sendMessage(Component.translatable("essentials.money.pay.received",
                        Argument.component("sender", sender),
                        Argument.numeric("amount", amount)
                ));
            });
        });
    }
}
