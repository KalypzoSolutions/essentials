package net.wandoria.essentials.util;

import org.bukkit.entity.Player;

/**
 * @author Tiko
 * 30.01.2025
 */
public class PermissionsRange {

    /**
     * Get the maximum value of a permission range
     *
     * @param player     player to check
     * @param permission permission prefix
     * @return max value or 0 if none
     */
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

    /**
     * Get the minimum value of a permission range
     *
     * @param player     player to check
     * @param permission permission prefix
     * @return -1 if none or min value
     */
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