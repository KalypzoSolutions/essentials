package de.kalypzo.essentials.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

/**
 * POJO that holds branding configuration including colors, prefixes, and a configured MiniMessage instance.
 * <p>
 * This class provides two constructors:
 * <ul>
 *     <li>Empty constructor: loads default values</li>
 *     <li>Constructor with ConfigurationSection: loads values from configuration</li>
 * </ul>
 *
 * @author einjojo
 */
public class BrandConfiguration {
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

    private final Component prefix;
    private final Component chatPrefix;
    private final TextColor blue;
    private final TextColor green;
    private final TextColor red;
    private final TextColor h1;
    private final TextColor h2;
    private final TextColor highlightColor;
    private final TextColor textColor;
    private final TextColor errorColor;
    private final TextColor successColor;
    private final MiniMessage miniMessage;

    /**
     * Default constructor that loads default branding values.
     */
    public BrandConfiguration() {
        this.prefix = MiniMessage.miniMessage().deserialize(DEFAULT_PREFIX_RAW);
        this.chatPrefix = MiniMessage.miniMessage().deserialize(DEFAULT_CHAT_PREFIX_RAW);
        this.blue = DEFAULT_BLUE;
        this.green = DEFAULT_GREEN;
        this.red = DEFAULT_RED;
        this.h1 = DEFAULT_H1;
        this.h2 = DEFAULT_H2;
        this.highlightColor = DEFAULT_HIGHLIGHT_COLOR;
        this.textColor = DEFAULT_TEXT_COLOR;
        this.errorColor = DEFAULT_ERROR_COLOR;
        this.successColor = DEFAULT_SUCCESS_COLOR;
        this.miniMessage = buildMiniMessage();
    }

    /**
     * Constructor that loads branding values from a configuration section.
     *
     * @param config the configuration section containing branding values
     * @param logger logger for warning messages about invalid colors
     */
    public BrandConfiguration(@NotNull ConfigurationSection config, Logger logger) {
        String prefixRaw = config.getString("prefix", DEFAULT_PREFIX_RAW);
        String chatPrefixRaw = config.getString("chat-prefix", DEFAULT_CHAT_PREFIX_RAW);
        this.prefix = MiniMessage.miniMessage().deserialize(prefixRaw);
        this.chatPrefix = MiniMessage.miniMessage().deserialize(chatPrefixRaw);

        this.blue = parseColor(config, "colors.blue", DEFAULT_BLUE, logger);
        this.green = parseColor(config, "colors.green", DEFAULT_GREEN, logger);
        this.red = parseColor(config, "colors.red", DEFAULT_RED, logger);
        this.h1 = parseColor(config, "colors.h1", DEFAULT_H1, logger);
        this.h2 = parseColor(config, "colors.h2", DEFAULT_H2, logger);
        this.highlightColor = parseColor(config, "colors.highlight", DEFAULT_HIGHLIGHT_COLOR, logger);
        this.textColor = parseColor(config, "colors.text", DEFAULT_TEXT_COLOR, logger);
        this.errorColor = parseColor(config, "colors.error", DEFAULT_ERROR_COLOR, logger);
        this.successColor = parseColor(config, "colors.success", DEFAULT_SUCCESS_COLOR, logger);

        this.miniMessage = buildMiniMessage();
    }

    private MiniMessage buildMiniMessage() {
        return MiniMessage.builder()
                .editTags(builder -> {
                    builder.tag("prefix", Tag.inserting(prefix));
                    builder.tag("chat", Tag.inserting(chatPrefix));
                    builder.tag("bl", Tag.styling(style -> style.color(blue)));
                    builder.tag("gr", Tag.styling(style -> style.color(green)));
                    builder.tag("re", Tag.styling(style -> style.color(red)));
                    builder.tag("hl", Tag.styling(style -> style.color(highlightColor)));
                    builder.tag("h1", Tag.styling(style -> style.color(h1).decorationIfAbsent(TextDecoration.BOLD, TextDecoration.State.TRUE)));
                    builder.tag("h2", Tag.styling(style -> style.color(h2)));
                    builder.tag("text", Tag.styling(style -> style.color(textColor).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)));
                    builder.tag("p", Tag.styling(style -> style.color(textColor).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)));
                    builder.tag("gray", Tag.styling(style -> style.color(textColor)));
                    builder.tag("ex", Tag.styling(style -> style.color(errorColor)));
                    builder.tag("ss", Tag.styling(style -> style.color(successColor)));
                })
                .build();
    }

    private static TextColor parseColor(ConfigurationSection config, String path, TextColor fallback, Logger logger) {
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
            logger.warn("Invalid color for {} in branding.yml: {}. Using default.", path, raw);
            return fallback;
        }
        for (int i = 0; i < normalized.length(); i++) {
            char c = normalized.charAt(i);
            boolean isHex = (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
            if (!isHex) {
                logger.warn("Invalid color for {} in branding.yml: {}. Using default.", path, raw);
                return fallback;
            }
        }
        int rgb = Integer.parseInt(normalized, 16);
        return TextColor.color(rgb);
    }

    public Component getPrefix() {
        return prefix;
    }

    public Component getChatPrefix() {
        return chatPrefix;
    }

    public TextColor getBlue() {
        return blue;
    }

    public TextColor getGreen() {
        return green;
    }

    public TextColor getRed() {
        return red;
    }

    public TextColor getH1() {
        return h1;
    }

    public TextColor getH2() {
        return h2;
    }

    public TextColor getHighlightColor() {
        return highlightColor;
    }

    public TextColor getTextColor() {
        return textColor;
    }

    public TextColor getErrorColor() {
        return errorColor;
    }

    public TextColor getSuccessColor() {
        return successColor;
    }

    public MiniMessage getMiniMessage() {
        return miniMessage;
    }
}
