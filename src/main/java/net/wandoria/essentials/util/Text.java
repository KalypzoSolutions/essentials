package net.wandoria.essentials.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;

public class Text {
    public static final Component PREFIX = MiniMessage.miniMessage().deserialize("<color:#7c86ff>Essentials</color> <color:#52525c>»</color><color:#cad5e2>");
    public static final Component CHAT_PREFIX = MiniMessage.miniMessage().deserialize("<color:#7c86ff>ChatSystem</color> <color:#52525c>»</color><color:#cad5e2>");
    public static final TextColor H1 = TextColor.color(0xffedd5); // orange-100
    public static final TextColor H2 = TextColor.color(0xfdba74); // orange-300
    public static final TextColor HIGHLIGHT_COLOR = TextColor.color(0xfb923c); // orange-400
    public static final TextColor TEXT_COLOR = TextColor.color(0xd4d4d4); // neutral-300
    public static final TextColor ERROR_COLOR = TextColor.color(0xf87171); // red-400
    public static final MiniMessage MINI_MESSAGE = MiniMessage.builder()
            .editTags(builder -> {
                builder.tag("prefix", Tag.inserting(PREFIX));
                builder.tag("chat", Tag.inserting(CHAT_PREFIX));
                builder.tag("hl", Tag.styling(style -> style.color(HIGHLIGHT_COLOR)));
                builder.tag("h1", Tag.styling(style -> style.color(H1)));
                builder.tag("h2", Tag.styling(style -> style.color(H2)));
                builder.tag("text", Tag.styling(style -> style.color(TEXT_COLOR).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)));
                builder.tag("p", Tag.styling(style -> style.color(TEXT_COLOR).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)));
                builder.tag("gray", Tag.styling(style -> style.color(TEXT_COLOR)));
                builder.tag("ex", Tag.styling(style -> style.color(ERROR_COLOR)));
            })
            .build();


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
}
