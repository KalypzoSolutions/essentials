package net.wandoria.essentials.command.world;

import net.wandoria.essentials.util.Text;
import net.wandoria.essentials.world.warps.WarpManager;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.paper.util.sender.PlayerSource;

@CommandContainer
public class SpawnCommand {

    @Command("spawn|hub|l")
    public void spawn(PlayerSource source) {
        var optWarp = WarpManager.getInstance().getWarp("spawn");
        optWarp.ifPresentOrElse(warp -> warp.teleport(source.source()),
                () -> {
                    source.source().sendMessage(Text.deserialize("<prefix> <ex>Der Warp 'spawn' wurde noch nicht gesetzt."));
                });
    }

}
