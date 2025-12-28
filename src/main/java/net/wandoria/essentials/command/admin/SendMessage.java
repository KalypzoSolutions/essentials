package net.wandoria.essentials.command.admin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import net.wandoria.essentials.user.EssentialsUser;
import net.wandoria.essentials.user.WildcardEssentialUser;
import net.wandoria.essentials.util.Text;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotation.specifier.Quoted;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Flag;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.paper.util.sender.Source;
import org.incendo.cloud.type.Either;

import java.text.BreakIterator;
import java.util.HashSet;
import java.util.Set;

@CommandContainer
public class SendMessage {
    private final Set<String> transactions = new HashSet<>();

    @Command("sendMiniMessage <receiver> <message>")
    @Permission("essentials.admin.sendminimessage")
    public void sendMiniMessage(Source source, Either<EssentialsUser, WildcardEssentialUser> receiver, @Quoted String message, @Flag("eggsound") boolean eggSound) {
        receiver.mapEither(user -> sendMiniMessage(user, message, eggSound), wildcard -> sendMiniMessage(wildcard, message));
        source.source().sendActionBar(Component.text("Message delivered ", NamedTextColor.GREEN));

    }

    private boolean sendMiniMessage(EssentialsUser receiver, String message, boolean eggSound) {
        receiver.sendMessage(Text.deserialize(message));
        receiver.playSound(Sound.ENTITY_CHICKEN_EGG, 1f, 1.4f);
        return true;
    }

    private boolean sendMiniMessage(WildcardEssentialUser receiver, String message) {
        receiver.sendMessage(Text.deserialize(message));
        return true;
    }


    @Command("sendPurchaseNotification <receiver> <transactionId>")
    @Permission("essentials.admin.sendpurchasenotification")
    public void sendPurchaseNotification(Source source, Player receiver, @Quoted String transactionId, @Flag("announce") boolean announce) {
        if (transactions.contains(transactionId)) {
            source.source().sendMessage(Text.deserialize("<ex>Kaufbenachrichtung wurde bereits gesendet!"));
            return;
        }
        // spawn firework
        receiver.playSound(receiver, Sound.ITEM_TOTEM_USE, 1f, 0.4f);
        receiver.playSound(receiver, Sound.ENTITY_PLAYER_LEVELUP, 1f, 0.4f);
        receiver.showTitle(Title.title(Text.deserialize("<green>Danke für deine Unterstützung"), Text.deserialize("<p>Deine Benefits sind auf dem Weg...")));
        source.source().sendActionBar(Component.text("delivered", NamedTextColor.GREEN));
        transactions.add(transactionId);

    }

}
