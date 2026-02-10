package de.kalypzo.essentials.command.user;


import de.kalypzo.essentials.command.CommandManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.translation.Argument;
import de.kalypzo.essentials.EssentialsPlugin;
import de.kalypzo.essentials.user.cooldown.RedisCooldownManager;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.paper.util.sender.PlayerSource;

import java.time.Duration;

/**
 *
 * <p>Feed and Heal</p>
 * <p>Because of @CommandContainer it gets instantiated by {@link CommandManager}</p>
 */

@CommandContainer
public class HealthCommand {

    public HealthCommand() {
        Permissions.registerAll();
    }

    @Command("feed")
    @CommandDescription("Fülle deinen Hunger")
    @Permission("essentials.command.feed")
    public void feed(PlayerSource source) {
        Duration remaining = FeedCooldown.INSTANCE.getCooldown(source.source().getUniqueId());
        if (remaining != null) {
            source.source().sendMessage(Component.translatable("essentials.feed.cooldown", cooldownArgument(remaining)));
            return;
        }
        Player player = source.source();
        if (!player.hasPermission(Permissions.FEED_BYPASS.permission)) {
            Duration cooldown;
            if (player.hasPermission(Permissions.FEED_15.permission)) {
                cooldown = Duration.ofMinutes(15);
            } else if (player.hasPermission(Permissions.FEED_30.permission)) {
                cooldown = Duration.ofMinutes(30);
            } else if (player.hasPermission(Permissions.FEED_60.permission)) {
                cooldown = Duration.ofMinutes(60);
            } else {
                EssentialsPlugin.instance().getComponentLogger().warn("Player {} tried to feed without any cooldown-permission.", player.getName());
                source.source().sendMessage(Component.translatable("essentials.cooldown.unsupported-permission"));
                return;
            }
            FeedCooldown.INSTANCE.setCooldown(player.getUniqueId(), cooldown);
        }
        source.source().setFoodLevel(20);
        player.playSound(player, Sound.ENTITY_PLAYER_BURP, 1, 1.2f);
        player.sendMessage(Component.translatable("essentials.feed.success"));
    }


    @Command("heal")
    @CommandDescription("Heile dich")
    @Permission("essentials.command.heal")
    public void heal(PlayerSource source) {
        Duration remaining = HealCooldown.INSTANCE.getCooldown(source.source().getUniqueId());
        if (remaining != null) {
            source.source().sendMessage(Component.translatable("essentials.heal.cooldown", cooldownArgument(remaining)));
            return;
        }
        if (!source.source().hasPermission(Permissions.HEAL_BYPASS.permission)) {
            Duration cooldown;
            if (source.source().hasPermission(Permissions.HEAL_30.permission)) {
                cooldown = Duration.ofMinutes(30);
            } else if (source.source().hasPermission(Permissions.HEAL_60.permission)) {
                cooldown = Duration.ofMinutes(60);
            } else {
                EssentialsPlugin.instance().getComponentLogger().warn("Player {} tried to heal without any cooldown-permission", source.source().getName());
                source.source().sendMessage(Component.translatable("essentials.heal.unsupported-permission"));
                return;
            }
            HealCooldown.INSTANCE.setCooldown(source.source().getUniqueId(), cooldown);
        }
        source.source().setHealth(20);
        source.source().setFoodLevel(20);
        source.source().playSound(source.source().getLocation(), Sound.ITEM_TOTEM_USE, 1, 1.2f);
        source.source().sendMessage(Component.translatable("essentials.heal.success"));
    }

    private enum Permissions {
        FEED_BYPASS("essentials.cooldown.feed.bypass", "Kann /feed ohne Cooldown benutzen"),
        HEAL_BYPASS("essentials.cooldown.heal.bypass", "Kann /heal ohne Cooldown benutzen"),
        FEED_15("essentials.cooldown.feed.cooldown-15", "Alle 15 Minuten /feed"),
        FEED_30("essentials.cooldown.feed.cooldown-30", "Alle 30 Minuten /feed"),
        FEED_60("essentials.cooldown.feed.cooldown-60", "Alle 60 Minuten /feed"),
        HEAL_60("essentials.cooldown.heal.cooldown-60", "Alle 60 Minuten /heal"),
        HEAL_30("essentials.cooldown.heal.cooldown-30", "Alle 30 Minuten /heal");

        public final String permission;
        public final String description;

        Permissions(String permission, String description) {
            this.permission = permission;
            this.description = description;
        }

        public static void registerAll() {
            for (Permissions perm : Permissions.values()) {
                Bukkit.getPluginManager().addPermission(new org.bukkit.permissions.Permission(perm.permission, perm.description));
            }
        }
    }

    private ComponentLike cooldownArgument(Duration remaining) {
        long delta = Math.max(remaining.toMillis(), 0); // remaining millis
        return Argument.component("cooldown", Component.text(DurationFormatUtils.formatDuration(delta, "mm'm' ss's'")));
    }

    /**
     * Lazy instance
     */
    private static final class FeedCooldown {
        private static final RedisCooldownManager INSTANCE = new RedisCooldownManager(EssentialsPlugin.instance().getRedis().connect(), "feed");
    }

    /**
     * Lazy instance
     */
    private static final class HealCooldown {
        private static final RedisCooldownManager INSTANCE = new RedisCooldownManager(EssentialsPlugin.instance().getRedis().connect(), "heal");
    }

}


