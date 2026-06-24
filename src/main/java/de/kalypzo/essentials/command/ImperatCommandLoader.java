package de.kalypzo.essentials.command;

import de.kalypzo.essentials.EssentialsPlugin;
import de.kalypzo.essentials.chat.emoji.EmojiGui;
import de.kalypzo.essentials.chat.emoji.EmojiRegistry;
import de.kalypzo.essentials.command.admin.BroadcastCommand;
import de.kalypzo.essentials.command.admin.DebugCommand;
import de.kalypzo.essentials.command.admin.LabyModDebugCommand;
import de.kalypzo.essentials.command.admin.SendMessage;
import de.kalypzo.essentials.command.chat.*;
import de.kalypzo.essentials.command.parser.*;
import de.kalypzo.essentials.command.plot.ProxiedPlotCommand;
import de.kalypzo.essentials.command.user.*;
import de.kalypzo.essentials.command.world.*;
import de.kalypzo.essentials.environment.PluginEnvironment;
import de.kalypzo.essentials.exception.ComponentException;
import de.kalypzo.essentials.exception.TransactionException;
import de.kalypzo.essentials.user.EssentialsOfflineUser;
import de.kalypzo.essentials.user.EssentialsUser;
import de.kalypzo.essentials.user.OnlineUsers;
import de.kalypzo.essentials.user.home.Home;
import de.kalypzo.essentials.util.Text;
import de.kalypzo.essentials.util.servername.InternalServerName;
import de.kalypzo.essentials.world.warps.Warp;
import io.lettuce.core.dynamic.annotation.Command;
import it.einjojo.economy.TransactionStatus;
import it.einjojo.economy.exception.EconomyException;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import studio.mevera.imperat.BukkitImperat;
import studio.mevera.imperat.command.suggestions.NativeAutoCompleterFactory;
import studio.mevera.imperat.exception.InvalidSyntaxException;
import studio.mevera.imperat.util.UsageFormatting;

import java.util.Locale;

/**
 * Creates commands using
 * <a href="https://mevera.studio/docs/Imperat/CreateYourFirstCommand">imperat</a>
 */
public class ImperatCommandLoader {
    private final EssentialsPlugin plugin;
    private final BukkitImperat imperat;

    public ImperatCommandLoader(EssentialsPlugin plugin) {
        this.plugin = plugin;
        imperat = BukkitImperat.builder(plugin, false)
                .autoCompleterFactory(new NativeAutoCompleterFactory<>(true))
                .build();
    }

    public void load() {
        registerArgTypes();
        registerErrorHandlers();
        registerCommands();
        plugin.getComponentLogger().info(Text.deserialize("<ss>Commands registriert"));
    }


    private void registerArgTypes() {
        PluginEnvironment env = plugin.getEnvironment();
        imperat.config().registerArgType(EssentialsUser.class, new EssentialsUserType(env));
        imperat.config().registerArgType(EssentialsOfflineUser.class, new EssentialsOfflineUserType(env));
        imperat.config().registerArgType(Home.class, new HomeType());
        imperat.config().registerArgType(Warp.class, new WarpType());
        imperat.config().registerArgType(GameMode.class, new GameModeType());
        imperat.config().registerArgType(OnlineUsers.class, new OnlineUsersType(env));
        imperat.config().registerArgType(PayTarget.class, new PayTargetType(env));
    }

    private void registerErrorHandlers() {
        imperat.config().setErrorHandler(ComponentException.class, (ex, ctx) ->
                ctx.source().origin().sendMessage(ex));
        imperat.config().setErrorHandler(EconomyException.class, (ex, ctx) -> {
            plugin.getSLF4JLogger().error("Economy error by {}", ctx.source().name(), ex);
            ctx.source().origin().sendMessage(Component.translatable("essentials.economy.error", Component.text("")));
        });
        imperat.config().setErrorHandler(InvalidSyntaxException.class, (e, ctx) -> {
            var player = ctx.source().asPlayer();
            if (player != null) {
                var suggestion = e.getClosestUsage();
                if (suggestion != null) {
                    String closestUsageFormat = UsageFormatting.formatClosestUsage(
                            ctx.imperatConfig().commandPrefix(),
                            ctx.getRootCommandLabelUsed(),
                            suggestion
                    );
                    player.sendMessage(Text.deserialize("<prefix> <p>Meintest du " + closestUsageFormat));
                } else {
                    player.sendMessage(Text.deserialize("<prefix> <ex>Ungültiger Befehl: <p>" + e.getInvalidUsage()));
                }
            } else {
                ctx.source().error(e.getMessage());
            }
        });
        imperat.config().setErrorHandler(TransactionException.class, (ex, ctx) -> {
            var status = ex.getStatus();
            if (status.equals(TransactionStatus.INSUFFICIENT_FUNDS)) {
                ctx.source().origin().sendMessage(Component.translatable("essentials.economy.insufficient-funds"));
            } else {
                ctx.source().origin().sendMessage(Component.translatable("essentials.economy.transaction-error",
                        Component.text(status.name().toUpperCase(Locale.ROOT))));
            }
        });
    }

    private void registerCommands() {
        imperat.registerCommands(
                new BroadcastCommand(),
                new DebugCommand(),
                new SendMessage(),
                new InfoCommand(),
                new DiscordCommand(),
                new ShopCommand(),
                new TeamChatCommand(),
                new MsgCommand(),
                new ReplyCommand(),
                new MsgCancelCommand(),
                new BackCommand(),
                new EnderchestCommand(),
                new FlyCommand(),
                new GameModeCommand(),
                new HeadCommand(),
                new FeedCommand(),
                new HealCommand(),
                new WorkbenchCommand(),
                new LoomCommand(),
                new AnvilCommand(),
                new StonecutterCommand(),
                new BalanceCommand(),
                new PayCommand(),
                new PlaytimeCommand(),
                new SettingsCommand(),
                new TogglePingCommand(),
                new SpeedCommand(),
                new TpaCommand(),
                new TpaAcceptCommand(),
                new HomeCommand(),
                new SetHomeCommand(),
                new DelHomeCommand(),
                new TeleportCommand(),
                new TeleportLocationCommand(),
                new WarpCommand(),
                new WarpsCommand(),
                new SpawnCommand(),
                new FarmweltCommand(),
                new NetherCommand()
        );

        if (plugin.getConfig().getBoolean("plot-cmd-proxy.enabled", true)) {
            String dest = plugin.getConfig().getString("plot-cmd-proxy.server", "citybuild");
            if (!dest.equals(InternalServerName.get())) {
                imperat.registerCommand(new ProxiedPlotCommand(dest));
                plugin.getComponentLogger().info(Text.deserialize("<ss>Registered Proxy-Plot command proxied to " + dest));
            }
        }

        if (plugin.getServer().getPluginManager().isPluginEnabled("LabyModServerAPI")) {
            imperat.registerCommand(new LabyModDebugCommand());
            plugin.getComponentLogger().info(Text.deserialize("<ss>Registered Labymod-Debug command"));
        }
    }
}
