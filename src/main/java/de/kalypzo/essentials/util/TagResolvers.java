package de.kalypzo.essentials.util;

import de.kalypzo.essentials.user.EssentialsUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.OfflinePlayer;

/**
 * Factory class for tag resolvers.
 */
public class TagResolvers {

    /**
     * name of the player.
     *
     * @return <pre>&lt;player&gt; resolver</pre>
     */
    public static TagResolver player(EssentialsUser essentialsUser) {
        return TagResolver.resolver("player", Tag.inserting(essentialsUser));
    }

    /**
     * name of the player.
     *
     * @return <pre>&lt;player&gt; resolver</pre>
     */
    public static TagResolver player(OfflinePlayer player) {
        String name = player.getName() == null ? "?" : player.getName();
        Component component = Component.text(name)
                .hoverEvent(Component.text(player.getUniqueId().toString(), Text.getTextColor()));
        return TagResolver.resolver("player", Tag.selfClosingInserting(component));
    }


}
