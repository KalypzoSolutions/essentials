package de.kalypzo.essentials;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Limited because of minimal invui.inventory-access import.
 */
public enum SupportedVersion {
    V1_21_11("1.21.11"),
    V1_21_4("1.21.4");

    private static final Set<String> SUPPORTED = Arrays.stream(values())
            .map(SupportedVersion::value)
            .collect(Collectors.toUnmodifiableSet());

    private final String value;

    SupportedVersion(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static boolean isSupported(JavaPlugin plugin) {
        return SUPPORTED.contains(plugin.getServer().getMinecraftVersion());
    }

    public static String supportedValues() {
        return Arrays.stream(values())
                .map(SupportedVersion::value)
                .collect(Collectors.joining(", "));
    }
}
