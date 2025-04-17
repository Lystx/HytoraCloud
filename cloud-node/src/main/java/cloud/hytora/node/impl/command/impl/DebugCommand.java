package cloud.hytora.node.impl.command.impl;

import cloud.hytora.context.annotations.ApplicationParticipant;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.command.annotation.Command;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.services.ICloudService;

@Command(
        value = "debug",
        description = "Dev Command"
)
@ApplicationParticipant
public class DebugCommand {

    @Command.Root
    public void executeDebug(CommandSender sender) {
        for (ICloudPlayer allCachedCloudPlayer : CloudDriver.getInstance().getPlayerManager().getAllCachedCloudPlayers()) {
            if (allCachedCloudPlayer.isOnline()) {
                ICloudService server = allCachedCloudPlayer.getServer();

                server.updateNametags();
                return;
            }
        }

    }
}
