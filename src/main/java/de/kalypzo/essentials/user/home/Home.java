package de.kalypzo.essentials.user.home;


import de.kalypzo.essentials.EssentialsPlugin;
import de.kalypzo.essentials.event.AsyncPlayerHomeTeleportEvent;
import de.kalypzo.essentials.world.NetworkPosition;
import de.kalypzo.essentials.world.TeleportExecutor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * @param owner    who owns the home
 * @param name     the name of the home
 * @param location network location
 */
@NullMarked
public record Home(UUID owner, String name, NetworkPosition location) implements TagResolver {
    public static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{1,64}$");

    public Home {
        if (!NAME_PATTERN.matcher(name).matches()) {
            throw new IllegalArgumentException("Invalid home name '" + name + "'");
        }

    }

    /**
     * Teleports the player to the home. Calls {@link AsyncPlayerHomeTeleportEvent} which can be canceled.
     * Gets executed asynchronously. If called from the main thread, it will be scheduled asynchronously.
     *
     * @param player the bukkit player to teleport
     */
    public void teleport(Player player) {
        if (Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTaskAsynchronously(EssentialsPlugin.instance(), () -> teleport(player));
        }
        if (!new AsyncPlayerHomeTeleportEvent(player, this).callEvent()) return;
        TeleportExecutor.getInstance().teleportPlayerToPosition(player.getUniqueId(), location);
    }

    /**
     *
     * @param name
     * @param arguments
     * @param ctx
     * @return
     * @throws ParsingException
     */
    @Override
    public @Nullable Tag resolve(String name, ArgumentQueue arguments, Context ctx) throws ParsingException {
        return switch (name.toLowerCase()) {
            case "home" -> Tag.selfClosingInserting(Component.text(name()));
            case "x" -> Tag.selfClosingInserting(Component.text((int) location.x()));
            case "y" -> Tag.selfClosingInserting(Component.text((int) location.y()));
            case "z" -> Tag.selfClosingInserting(Component.text((int) location.z()));
            case "world" -> Tag.selfClosingInserting(Component.text(location.worldName()));
            case "server" -> Tag.selfClosingInserting(Component.text(location.serverName()));
            default -> null;
        };
    }

    @Override
    public boolean has(String name) {
        return "home".equals(name) || "x".equals(name) || "y".equals(name) || "z".equals(name) || "world".equals(name) || "server".equals(name);
    }
}
