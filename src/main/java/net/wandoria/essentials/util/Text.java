package net.wandoria.essentials.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class Text {
    public static final Component PREFIX = MiniMessage.miniMessage().deserialize("<color:#7c86ff>Essentials</color> <color:#52525c>»</color> <color:#cad5e2>");
    public static final Component CHAT_PREFIX = MiniMessage.miniMessage().deserialize("<color:#7c86ff>ChatSystem</color> <color:#52525c>»</color> <color:#cad5e2>");
    public static final TextColor HIGHLIGHT = TextColor.color(0x00bcff);
    public static MiniMessage MINI_MESSAGE = MiniMessage.builder().editTags(builder -> {
        builder.tag("prefix", Tag.inserting(PREFIX));
        builder.tag("chat", Tag.inserting(CHAT_PREFIX));
        builder.tag("hl", Tag.styling(HIGHLIGHT));
        builder.tag("ss", Tag.styling(TextColor.color(0x7bf1a8)));
        builder.tag("ex", Tag.styling(TextColor.color(0xff6467)));
    }).build();


    public static Component deserialize(String miniMessage, TagResolver... args) {
        return MINI_MESSAGE.deserialize(miniMessage, args);
    }

}
