package de.kalypzo.essentials.command.chat;

import de.kalypzo.essentials.util.Text;
import studio.mevera.imperat.BukkitCommandSource;
import studio.mevera.imperat.annotations.types.Execute;
import studio.mevera.imperat.annotations.types.RootCommand;

@RootCommand("discord")
public class DiscordCommand {

    @Execute
    public void discord(BukkitCommandSource source) {
        source.origin().sendMessage(Text.deserialize("<newline><prefix> <hl>https://discord.gg/totorix<newline>"));
    }
}
