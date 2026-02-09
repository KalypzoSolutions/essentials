package de.kalypzo.essentials.command.user;

import com.destroystokyo.paper.profile.PlayerProfile;
import de.kalypzo.essentials.command.CommandManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import de.kalypzo.essentials.EssentialsPlugin;
import de.kalypzo.essentials.user.cooldown.RedisCooldownManager;
import de.kalypzo.essentials.util.MainThreadUtil;
import de.kalypzo.essentials.util.PermissionsRange;
import de.kalypzo.essentials.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.paper.util.sender.PlayerSource;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 *
 * <p>Skull with x-delay</p>
 * <p>Because of @CommandContainer it gets instantiated by {@link CommandManager}</p>
 */
@CommandContainer
public class HeadCommand {

    public HeadCommand() {
        Bukkit.getPluginManager().addPermission(new org.bukkit.permissions.Permission("essentials.cooldown.head.bypass"));
    }

    private static class LAZY {
        private static final RedisCooldownManager INSTANCE = new RedisCooldownManager(EssentialsPlugin.instance().getRedis().connect(), "head");
    }


    /**
     * Gives the player a head of the specified player
     *
     * @param source bypasses cooldown, if it has permission essentials.cooldown.head.bypass
     * @param player the skull texture provider
     * @return a future completing, when the head has been given
     */
    @Command("head|skull <player>")
    @CommandDescription("Gibt dir den Kopf eines Spielers")
    @Permission("essentials.command.head")
    public CompletableFuture<Void> giveHead(PlayerSource source, OfflinePlayer player) {
        // check cooldown
        Player sender = source.source();
        if (!source.source().hasPermission("essentials.cooldown.head.bypass")) {
            return CompletableFuture.runAsync(() -> {
                Duration duration = LAZY.INSTANCE.getCooldown(player.getUniqueId());
                if (duration != null) {
                    sender.sendMessage(Text.deserialize("<prefix> <p>Du kannst in <duration> einen weiteren Kopf holen!",
                            Placeholder.component("duration", Text.durationComponent(duration))));
                    return;
                }
                int cooldownDays = PermissionsRange.min(source.source(), "essentials.cooldown.head");
                if (cooldownDays == -1) {
                    EssentialsPlugin.instance().getComponentLogger().warn("Player {}({}) has no cooldown permission configured and does not have a bypass", sender.getUniqueId(), sender.getName());
                }
                LAZY.INSTANCE.setCooldown(sender.getUniqueId(), Duration.ofDays(cooldownDays));
            }, EssentialsPlugin.getExecutorService());
        }
        return giveHeadInternal(sender, player);
    }

    private CompletableFuture<Void> giveHeadInternal(Player sender, OfflinePlayer player) {
        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
        final SkullMeta meta = (SkullMeta) playerHead.getItemMeta();
        PlayerProfile profile = player.getPlayerProfile();
        return profile.update().thenAcceptAsync(completedProfile -> {
            String name = player.getName();
            meta.customName(Component.text(name != null ? name : "?", Text.HIGHLIGHT_COLOR));
            playerHead.setItemMeta(meta);
        }, MainThreadUtil.createExecutor(sender));
    }


}

