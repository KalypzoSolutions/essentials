package de.kalypzo.essentials.gui;

import de.kalypzo.essentials.util.Text;
import lombok.Setter;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import xyz.xenondevs.invui.item.builder.AbstractItemBuilder;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.builder.SkullBuilder;

/**
 * Creates an item builder based on the config
 */
@Setter
public class ItemBuilderConfigReader {
    public String skullTexture;


    public AbstractItemBuilder<?> parse(ConfigurationSection itemSection, TagResolver... resolvers) {
        Material material = Material.getMaterial(itemSection.getString("material", "STONE").toUpperCase());
        if (material == null) {
            throw new IllegalArgumentException("Invalid material specified in item configuration: " + itemSection.getString("material"));
        }

        String base64 = itemSection.getString("skull_texture", skullTexture);
        AbstractItemBuilder<?> builder;
        if (base64 != null) {
            builder = new SkullBuilder(new SkullBuilder.HeadTexture(base64));
        } else {
            builder = new ItemBuilder(material);
        }

        if (itemSection.contains("amount")) {
            builder.setAmount(itemSection.getInt("amount"));
        }
        if (itemSection.contains("name")) {
            builder.setDisplayName(Text.gui(itemSection.getString("name"), resolvers));
        }
        if (itemSection.contains("lore")) {
            builder.setLore(Text.guiLore(itemSection.getStringList("lore"), resolvers));
        }
        boolean hideLore = itemSection.getBoolean("hide_tooltip", false);
        if (hideLore) {
            builder.addModifier(is -> {
                is.editMeta(meta -> {
                    meta.setHideTooltip(true);
                });
                return is;
            });
        }
        if (itemSection.contains("item_model")) {
            String modelStr = itemSection.getString("item_model");
            if (modelStr == null) {
                throw new IllegalArgumentException("Invalid item_model specified in item configuration");
            }
            try {
                NamespacedKey modelKey = NamespacedKey.fromString(modelStr);
                builder.addModifier(is -> {
                    is.editMeta(meta -> {
                        meta.setItemModel(modelKey);
                    });
                    return is;
                });
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid item model specified in item configuration: " + modelStr, e);
            }
        }
        return builder;
    }

}
