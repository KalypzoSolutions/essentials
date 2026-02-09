package de.kalypzo.essentials.util;

import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import de.kalypzo.essentials.user.EssentialsUser;
import org.bukkit.entity.Player;

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
    public static TagResolver player(Player player) {
        return TagResolver.resolver("player", Tag.inserting(player.name()));
    }


}
