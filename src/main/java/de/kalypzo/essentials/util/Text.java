package de.kalypzo.essentials.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.time.Duration;

/**
 * Text Utilities when working with mini-messages.
 * <p>Provides InvUI Utility Methods prefixed with <code>gui</code></p>
 *
 * <hr>
 * <p>The minimessage instance has the following supported Tags</p>
 * <ul>
 *     <li>&lt;prefix&gt; message prefix</li>
 *     <li>&lt;bl&gt; desaturated blue color</li>
 *     <li>&lt;gr&gt; desaturated green color</li>
 *     <li>&lt;re&gt; desaturated red color</li>
 *     <li>&lt;hl&gt; primary highlight color</li>
 *     <li>&lt;h1&gt; almost white with primary accent and bold</li>
 *     <li>&lt;h2&gt; primary accent color</li>
 *     <li>&lt;text&gt;, &lt;p&gt;, &lt;text&gt; light gray</li>
 *     <li>&lt;ss&gt; success color (green)</li>
 *     <li>&lt;ex&gt; error color (red)</li>
 * </ul>
 * <hr>
 * <p>When using this class some constants like <code>PREFIX</code> have to be modified.</p>
 *
 * @author einjojo
 * @version 1.3 (adjusted for essentials)
 */
public class Text {
    private static final String BRANDING_FILE_NAME = "branding.yml";
    private static volatile BrandConfiguration brandConfiguration = new BrandConfiguration();

    public static synchronized void loadBranding(JavaPlugin plugin) {
        File brandingFile = new File(plugin.getDataFolder(), BRANDING_FILE_NAME);
        if (!brandingFile.exists()) {
            plugin.saveResource(BRANDING_FILE_NAME, false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(brandingFile);
        brandConfiguration = new BrandConfiguration(config, plugin.getSLF4JLogger());
    }

    public static Component getPrefix() {
        return brandConfiguration.getPrefix();
    }

    public static Component getChatPrefix() {
        return brandConfiguration.getChatPrefix();
    }

    public static TextColor getBlue() {
        return brandConfiguration.getBlue();
    }

    public static TextColor getGreen() {
        return brandConfiguration.getGreen();
    }

    public static TextColor getRed() {
        return brandConfiguration.getRed();
    }

    public static TextColor getH1() {
        return brandConfiguration.getH1();
    }

    public static TextColor getH2() {
        return brandConfiguration.getH2();
    }

    public static TextColor getHighlightColor() {
        return brandConfiguration.getHighlightColor();
    }

    public static TextColor getTextColor() {
        return brandConfiguration.getTextColor();
    }

    public static TextColor getErrorColor() {
        return brandConfiguration.getErrorColor();
    }

    public static TextColor getSuccessColor() {
        return brandConfiguration.getSuccessColor();
    }

    public static MiniMessage getMiniMessage() {
        return brandConfiguration.getMiniMessage();
    }

    public static @NotNull Component deserialize(String s, TagResolver... resolvers) {
        if (s == null || s.isEmpty()) {
            return Component.empty();
        }
        return brandConfiguration.getMiniMessage().deserialize(s, resolvers);
    }

    public static @NotNull Component smallCaps(String singleMiniMessage, TagResolver... resolvers) {
        if (singleMiniMessage == null || singleMiniMessage.isEmpty()) {
            return Component.empty();
        }
        return brandConfiguration.getMiniMessage().deserialize(asTinyCaps(singleMiniMessage), resolvers);

    }

    public static String asTinyCaps(String s) {
        StringBuilder sb = new StringBuilder();
        boolean isMiniMessageTag = false;
        for (char c : s.toCharArray()) {
            if (c == '<') {
                isMiniMessageTag = true;
            } else if (c == '>') {
                isMiniMessageTag = false;
            }
            if (isMiniMessageTag) {
                sb.append(c);
            } else {
                sb.append(getTinyChar(c));
            }
        }
        return sb.toString();
    }

    /**
     * Converts a character to its tiny text equivalent.
     * Uses a switch expression on the lowercase character for efficient lookup.
     *
     * @param original The character to convert.
     * @return The tiny text character, or the original character if no conversion exists.
     */
    private static char getTinyChar(char original) {
        char lower = Character.toLowerCase(original);

        // Switch expression for conversion logic.
        // This replaces the static Map lookup.
        return switch (lower) {
            case 'a' -> 'ᴀ';
            case 'b' -> 'ʙ';
            case 'c' -> 'ᴄ';
            case 'd' -> 'ᴅ';
            case 'e' -> 'ᴇ';
            case 'f' -> 'ꜰ';
            case 'g' -> 'ɢ';
            case 'h' -> 'ʜ';
            case 'i' -> 'ɪ';
            case 'j' -> 'ᴊ';
            case 'k' -> 'ᴋ';
            case 'l' -> 'ʟ';
            case 'm' -> 'ᴍ';
            case 'n' -> 'ɴ';
            case 'o' -> 'ᴏ';
            case 'p' -> 'ᴘ';
            case 'q' -> 'ꞯ';
            case 'r' -> 'ʀ';
            case 's' -> 's'; // Original mapping
            case 't' -> 'ᴛ';
            case 'u' -> 'ᴜ';
            case 'v' -> 'ᴠ';
            case 'w' -> 'ᴡ';
            case 'x' -> 'x'; // Original mapping
            case 'y' -> 'ʏ';
            case 'z' -> 'ᴢ';

            case '0' -> '₀';
            case '1' -> '₁';
            case '2' -> '₂';
            case '3' -> '₃';
            case '4' -> '₄';
            case '5' -> '₅';
            case '6' -> '₆';
            case '7' -> '₇';
            case '8' -> '₈';
            case '9' -> '₉';

            // Default case: return the original character if no tiny equivalent is found.
            // This behavior matches the original `LETTERS.getOrDefault(c, c)` logic.
            default -> original;
        };
    }

    /**
     * Turns a duration into a human-readable component
     *
     * @param duration duration
     * @return component
     */
    public static @NotNull Component durationComponent(Duration duration) {
        if (duration == null) {
            return Component.text("Permanent");
        }
        TextComponent.Builder builder = Component.text();
        if (duration.toDaysPart() > 0) {
            builder.append(Component.text(duration.toDaysPart(), brandConfiguration.getTextColor()));
        }
        if (duration.toHoursPart() > 0) {
            if (duration.toDaysPart() > 0) {
                builder.append(Component.space());
            }
            builder.append(Component.text(duration.toHoursPart(), brandConfiguration.getTextColor()));
            builder.append(Component.text("h", brandConfiguration.getTextColor()));
        }
        if (duration.toMinutesPart() > 0) {
            if (duration.toHoursPart() > 0) {
                builder.append(Component.space());
            }
            builder.append(Component.text(duration.toMinutesPart(), brandConfiguration.getTextColor()));
            builder.append(Component.text("m", brandConfiguration.getTextColor()));
        }
        if (duration.toSecondsPart() > 0) {
            if (duration.toMinutesPart() > 0) {
                builder.append(Component.space());
            }
            builder.append(Component.text(duration.toSecondsPart(), brandConfiguration.getTextColor()));
            builder.append(Component.text("s", brandConfiguration.getTextColor()));
        }
        return builder.build();
    }
}
