package net.wandoria.essentials.world;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.wandoria.essentials.EssentialsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

/**
 * Describes a position on the server network
 *
 * @param serverName the server name where the position is located
 * @param worldName  the world name where the position is located
 * @param x          x
 * @param y          y
 * @param z          z
 * @param yaw        yaw
 * @param pitch      pitch
 */
public record NetworkPosition(@NotNull String serverName,
                              @NotNull String worldName,
                              double x,
                              double y,
                              double z,
                              float yaw,
                              float pitch) {


    /**
     * Serialize the NetworkPosition to a string with semicolon as a separator.
     * Can be deserialized with {@link #deserialize(String)}
     *
     * @return serialized string
     */
    public String serialize() {
        return String.join(";", serverName, worldName, String.valueOf(x), String.valueOf(y), String.valueOf(z), String.valueOf(yaw), String.valueOf(pitch));
    }

    /**
     * Deserialize a serialized NetworkPosition
     *
     * @param serialized serialized string
     * @return deserialized NetworkPosition
     */
    public static NetworkPosition deserialize(String serialized) {
        String[] parts = serialized.split(";");
        if (parts.length != 7) {
            throw new IllegalArgumentException("Invalid serialized NetworkPosition: " + serialized);
        }
        String serverName = parts[0];
        String worldName = parts[1];
        double x = Double.parseDouble(parts[2]);
        double y = Double.parseDouble(parts[3]);
        double z = Double.parseDouble(parts[4]);
        float yaw = Float.parseFloat(parts[5]);
        float pitch = Float.parseFloat(parts[6]);
        return new NetworkPosition(serverName, worldName, x, y, z, yaw, pitch);
    }

    public static NetworkPosition fromLocation(Location location, String serverName) {
        return new NetworkPosition(serverName, location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    /**
     * Create an instance of NetworkPosition from a Location object with the server name.
     *
     * @param location the location to convert
     * @return a NetworkPosition object with the current server name and provided the location data
     */
    public static NetworkPosition createByLocation(Location location) {
        if (location == null) {
            throw new IllegalArgumentException("Location is null");
        }
        String serverName = EssentialsPlugin.environment().getServerName();
        return new NetworkPosition(serverName, location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    /**
     * Turns this NetworkPosition into a Bukkit Location object.
     *
     * @return the Location object where the world might be null.
     */
    public Location toLocation() {
        World world = Bukkit.getWorld(worldName);
        return new Location(world, x, y, z, yaw, pitch);
    }

    /**
     * Create a TagResolver for this NetworkPosition.
     *
     * @return the TagResolver with "x", "y", "z", "yaw", "pitch", "world" and "server" tags.
     */
    public TagResolver asTagResolver() {
        NetworkPosition pos = this;
        return TagResolver.builder()
                .tag("x", Tag.inserting(Component.text(pos.x())))
                .tag("y", Tag.inserting(Component.text(pos.y())))
                .tag("z", Tag.inserting(Component.text(pos.z())))
                .tag("yaw", Tag.inserting(Component.text(pos.yaw())))
                .tag("pitch", Tag.inserting(Component.text(pos.pitch())))
                .tag("world", Tag.inserting(Component.text(pos.worldName())))
                .tag("server", Tag.inserting(Component.text(pos.serverName())))
                .build();

    }


}
