package de.kalypzo.essentials.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
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
    private static final String DEFAULT_PREFIX_RAW = "<color:#F16D34>Essentials</color> <color:#52525c>»</color><color:#d4d4d4>";
    private static final String DEFAULT_CHAT_PREFIX_RAW = "<color:#7c86ff>ChatSystem</color> <color:#52525c>»</color><color:#cad5e2>";

    private static final TextColor DEFAULT_BLUE = TextColor.color(0x00bcff);
    private static final TextColor DEFAULT_GREEN = TextColor.color(0x7bf1a8);
    private static final TextColor DEFAULT_RED = TextColor.color(0xff6467);
    private static final TextColor DEFAULT_H1 = TextColor.color(0xffedd5); // orange-100
    private static final TextColor DEFAULT_H2 = TextColor.color(0xfdba74); // orange-300
    private static final TextColor DEFAULT_HIGHLIGHT_COLOR = TextColor.color(0xfb923c); // orange-400
    private static final TextColor DEFAULT_TEXT_COLOR = TextColor.color(0xd4d4d4); // neutral-300
    private static final TextColor DEFAULT_ERROR_COLOR = TextColor.color(0xf87171); // red-400
    private static final TextColor DEFAULT_SUCCESS_COLOR = TextColor.color(0x05df72); // green-400

    public static volatile Component PREFIX = MiniMessage.miniMessage().deserialize(DEFAULT_PREFIX_RAW);
    public static volatile Component CHAT_PREFIX = MiniMessage.miniMessage().deserialize(DEFAULT_CHAT_PREFIX_RAW);
    public static volatile TextColor BLUE = DEFAULT_BLUE;
    public static volatile TextColor GREEN = DEFAULT_GREEN;
    public static volatile TextColor RED = DEFAULT_RED;
    public static volatile TextColor H1 = DEFAULT_H1;
    public static volatile TextColor H2 = DEFAULT_H2;
    public static volatile TextColor HIGHLIGHT_COLOR = DEFAULT_HIGHLIGHT_COLOR;
    public static volatile TextColor TEXT_COLOR = DEFAULT_TEXT_COLOR;
    public static volatile TextColor ERROR_COLOR = DEFAULT_ERROR_COLOR;
    public static volatile TextColor SUCCESS_COLOR = DEFAULT_SUCCESS_COLOR;
    public static volatile MiniMessage MINI_MESSAGE = buildMiniMessage();

    public static void loadBranding(JavaPlugin plugin) {
        File brandingFile = new File(plugin.getDataFolder(), BRANDING_FILE_NAME);
        if (!brandingFile.exists()) {
            plugin.saveResource(BRANDING_FILE_NAME, false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(brandingFile);

        String prefixRaw = config.getString("prefix", DEFAULT_PREFIX_RAW);
        String chatPrefixRaw = config.getString("chat-prefix", DEFAULT_CHAT_PREFIX_RAW);
        PREFIX = MiniMessage.miniMessage().deserialize(prefixRaw);
        CHAT_PREFIX = MiniMessage.miniMessage().deserialize(chatPrefixRaw);

        BLUE = parseColor(config, "colors.blue", DEFAULT_BLUE, plugin);
        GREEN = parseColor(config, "colors.green", DEFAULT_GREEN, plugin);
        RED = parseColor(config, "colors.red", DEFAULT_RED, plugin);
        H1 = parseColor(config, "colors.h1", DEFAULT_H1, plugin);
        H2 = parseColor(config, "colors.h2", DEFAULT_H2, plugin);
        HIGHLIGHT_COLOR = parseColor(config, "colors.highlight", DEFAULT_HIGHLIGHT_COLOR, plugin);
        TEXT_COLOR = parseColor(config, "colors.text", DEFAULT_TEXT_COLOR, plugin);
        ERROR_COLOR = parseColor(config, "colors.error", DEFAULT_ERROR_COLOR, plugin);
        SUCCESS_COLOR = parseColor(config, "colors.success", DEFAULT_SUCCESS_COLOR, plugin);

        MINI_MESSAGE = buildMiniMessage();
    }

    private static MiniMessage buildMiniMessage() {
        return MiniMessage.builder()
                .editTags(builder -> {
                    builder.tag("prefix", Tag.inserting(PREFIX));
                    builder.tag("chat", Tag.inserting(CHAT_PREFIX));
                    builder.tag("bl", Tag.styling(style -> style.color(BLUE)));
                    builder.tag("gr", Tag.styling(style -> style.color(GREEN)));
                    builder.tag("re", Tag.styling(style -> style.color(RED)));
                    builder.tag("hl", Tag.styling(style -> style.color(HIGHLIGHT_COLOR)));
                    builder.tag("h1", Tag.styling(style -> style.color(H1).decorationIfAbsent(TextDecoration.BOLD, TextDecoration.State.TRUE)));
                    builder.tag("h2", Tag.styling(style -> style.color(H2)));
                    builder.tag("text", Tag.styling(style -> style.color(TEXT_COLOR).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)));
                    builder.tag("p", Tag.styling(style -> style.color(TEXT_COLOR).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)));
                    builder.tag("gray", Tag.styling(style -> style.color(TEXT_COLOR)));
                    builder.tag("ex", Tag.styling(style -> style.color(ERROR_COLOR)));
                    builder.tag("ss", Tag.styling(style -> style.color(SUCCESS_COLOR)));
                })
                .build();
    }

    private static TextColor parseColor(FileConfiguration config, String path, TextColor fallback, JavaPlugin plugin) {
        String raw = config.getString(path);
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        String normalized = raw.trim();
        if (normalized.startsWith("#")) {
            normalized = normalized.substring(1);
        } else if (normalized.startsWith("0x") || normalized.startsWith("0X")) {
            normalized = normalized.substring(2);
        }
        if (normalized.length() != 6) {
            plugin.getSLF4JLogger().warn("Invalid color for {} in {}: {}. Using default.", path, BRANDING_FILE_NAME, raw);
            return fallback;
        }
        for (int i = 0; i < normalized.length(); i++) {
            char c = normalized.charAt(i);
            boolean isHex = (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
            if (!isHex) {
                plugin.getSLF4JLogger().warn("Invalid color for {} in {}: {}. Using default.", path, BRANDING_FILE_NAME, raw);
                return fallback;
            }
        }
        int rgb = Integer.parseInt(normalized, 16);
        return TextColor.color(rgb);
    }

    public static @NotNull Component deserialize(String s, TagResolver... resolvers) {
        if (s == null || s.isEmpty()) {
            return Component.empty();
        }
        return MINI_MESSAGE.deserialize(s, resolvers);
    }

    public static @NotNull Component smallCaps(String singleMiniMessage, TagResolver... resolvers) {
        if (singleMiniMessage == null || singleMiniMessage.isEmpty()) {
            return Component.empty();
        }
        return MINI_MESSAGE.deserialize(asTinyCaps(singleMiniMessage), resolvers);

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
            builder.append(Component.text(duration.toDaysPart(), TEXT_COLOR));
        }
        if (duration.toHoursPart() > 0) {
            if (duration.toDaysPart() > 0) {
                builder.append(Component.space());
            }
            builder.append(Component.text(duration.toHoursPart(), TEXT_COLOR));
            builder.append(Component.text("h", TEXT_COLOR));
        }
        if (duration.toMinutesPart() > 0) {
            if (duration.toHoursPart() > 0) {
                builder.append(Component.space());
            }
            builder.append(Component.text(duration.toMinutesPart(), TEXT_COLOR));
            builder.append(Component.text("m", TEXT_COLOR));
        }
        if (duration.toSecondsPart() > 0) {
            if (duration.toMinutesPart() > 0) {
                builder.append(Component.space());
            }
            builder.append(Component.text(duration.toSecondsPart(), TEXT_COLOR));
            builder.append(Component.text("s", TEXT_COLOR));
        }
        return builder.build();
    }
}
