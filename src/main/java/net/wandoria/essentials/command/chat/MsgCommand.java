package net.wandoria.essentials.command.chat;


import net.kyori.adventure.text.Component;
import net.wandoria.essentials.EssentialsPlugin;
import net.wandoria.essentials.chat.ChatSystem;
import net.wandoria.essentials.chat.PrivateMessageResult;
import net.wandoria.essentials.exception.ComponentException;
import net.wandoria.essentials.user.EssentialsUser;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.paper.util.sender.PlayerSource;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * <p>Provides all commands related to private messages / reply </p>
 * <p>Because of @CommandContainer it gets instantiated by {@link net.wandoria.essentials.command.CommandManager}</p>
 */
@CommandContainer
public class MsgCommand {
    private final ChatSystem chatSystem = EssentialsPlugin.instance().getChatSystem();
    private final JavaPlugin schedulerExecutor = EssentialsPlugin.instance();


    @Command("msg <receiver> <message>")
    @Permission("essentials.command.msg")
    public CompletableFuture<Void> msg(PlayerSource sender, EssentialsUser receiver, @Greedy String message) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        Bukkit.getServer().getScheduler().runTaskAsynchronously(schedulerExecutor, () -> {
            try {
                if (receiver.getUniqueId().equals(sender.source().getUniqueId())) {
                    throw ComponentException.translatable("essentials.chat.no-self-msg");
                }
                handlePrivateMessageResult(chatSystem.sendPrivateMessage(sender.source(), receiver, message), future, receiver.getName());
            } catch (Exception ex) {
                future.completeExceptionally(ex);
            }
        });
        return future;
    }

    @Command("r|reply <message>")
    @Permission("essentials.command.reply")
    public CompletableFuture<Void> reply(PlayerSource sender, @Greedy String message) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        Bukkit.getServer().getScheduler().runTaskAsynchronously(schedulerExecutor, () -> {
            PrivateMessageResult result = chatSystem.replyToLastMessage(sender.source(), message);
            handlePrivateMessageResult(result, future, null);
        });
        return future;
    }

    @Command("msgcancel|msgtoggle")
    @Permission("essentials.command.msgcancel")
    @CommandDescription("Unterbinde das empfangen privater Nachrichten")
    public CompletableFuture<Void> cancelMsg(PlayerSource sender) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        Bukkit.getServer().getScheduler().runTaskAsynchronously(schedulerExecutor, () -> {
            boolean disabledCurrently = chatSystem.hasDisabledPrivateMessages(sender.source().getUniqueId());
            chatSystem.setPrivateMessagesDisabled(sender.source().getUniqueId(), !disabledCurrently);
            if (disabledCurrently) {
                sender.source().sendMessage(Component.translatable("essentials.chat.msgcancel-enabled"));
            } else {
                sender.source().sendMessage(Component.translatable("essentials.chat.msgcancel-disabled"));
            }
            future.complete(null);
        });
        return future;
    }

    private void handlePrivateMessageResult(PrivateMessageResult result, CompletableFuture<Void> future, @Nullable String receiverName) {
        switch (result) {
            case SUCCESS -> future.complete(null);
            case RECEIVER_DISABLED_PRIVATE_MESSAGES ->
                    future.completeExceptionally(ComponentException.translatable("essentials.chat.msg-disabled", receiverName));
            case RECEIVER_IS_OFFLINE ->
                    future.completeExceptionally(ComponentException.translatable("essentials.user.offline"));
            case SENDER_MUST_NOT_BE_RECEIVER ->
                    future.completeExceptionally(ComponentException.translatable("essentials.chat.no-self-msg"));

        }
    }
}
