package cloud.hytora.modules.hubcommand.command;

import cloud.hytora.driver.command.annotation.Command;
import cloud.hytora.driver.command.sender.PlayerCommandSender;
import cloud.hytora.driver.common.CloudMessages;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.player.executor.PlayerExecutor;

import static cloud.hytora.driver.command.CommandScope.INGAME_HOSTED_ON_CLOUD_SIDE;

@Command(
        value = {"hub", "lobby", "l"},
        description = "Sends you to a fallback",
        executionScope = INGAME_HOSTED_ON_CLOUD_SIDE
)
public class HubCommand {


    @Command.Root
    public void execute(PlayerCommandSender sender) {
        ICloudPlayer player = sender.getPlayer();
        PlayerExecutor executor = PlayerExecutor.forPlayer(player);

        player.getServerAsync()
                .onTaskFailed(e -> {
                    player.sendMessage("§cCouldn't send you to a fallback!");
                })
                .onTaskSucess(server -> {

                    if (server.isRegisteredAsFallback()) {
                        player.sendMessage(CloudMessages.getInstance().getAlreadyOnFallbackMessage());
                        return;
                    }
                    executor.sendToFallback();
                });
    }
}
