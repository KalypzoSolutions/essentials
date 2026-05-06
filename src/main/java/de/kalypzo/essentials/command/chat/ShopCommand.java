package de.kalypzo.essentials.command.chat;

import de.kalypzo.essentials.util.Text;
import studio.mevera.imperat.BukkitCommandSource;
import studio.mevera.imperat.annotations.types.Execute;
import studio.mevera.imperat.annotations.types.RootCommand;

@RootCommand({"store", "shop"})
public class ShopCommand {

    @Execute
    public void shop(BukkitCommandSource source) {
        source.origin().sendMessage(Text.deserialize("<newline><prefix> <hl>https://totorix.shop<newline>"));
    }
}
