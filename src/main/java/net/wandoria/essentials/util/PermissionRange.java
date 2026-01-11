package net.wandoria.essentials.util;

import org.bukkit.entity.Player;

/**
 * @author Tiko
 */
public class PermissionRange {

    public static int max(final Player player, final String permission) {
        int max = 0;
        for (final var perm : player.getEffectivePermissions()) {
            final var permName = perm.getPermission();

            if (permName.startsWith(permission + ".")) {
                try {
                    final var allowed = Integer.parseInt(permName.substring((permission + ".").length()));
                    if (allowed > max) {
                        max = allowed;
                    }
                } catch (final NumberFormatException ignored) {
                }
            }
        }
        return max;
    }

    public static int min(final Player player, final String permission) {
        Integer min = null;
        for (final var perm : player.getEffectivePermissions()) {
            final var permName = perm.getPermission();
            if (permName.startsWith(permission + ".")) {
                try {
                    final var value = Integer.parseInt(permName.substring((permission + ".").length()));
                    if (value > 0 && (min == null || value < min)) {
                        min = value;
                    }
                } catch (final NumberFormatException ignored) {
                }
            }
        }
        return (min != null) ? min : -1;
    }
}