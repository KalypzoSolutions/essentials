package de.kalypzo.essentials.chat.emoji;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EmojiRegistry {
    private final List<Emoji> emojis = new ArrayList<>();

    public void register(Emoji emoji) { emojis.add(emoji); }
    public void clear() { emojis.clear(); }
    public List<Emoji> getAll() { return Collections.unmodifiableList(emojis); }
}