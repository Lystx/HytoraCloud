package cloud.hytora.modules.hubcommand.command;

import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.command.annotation.Command;
import cloud.hytora.driver.command.annotation.CommandDescription;
import cloud.hytora.driver.command.annotation.CommandExecutionScope;
import cloud.hytora.driver.command.annotation.Root;
import cloud.hytora.driver.command.sender.PlayerCommandSender;
import cloud.hytora.driver.common.CloudMessages;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.player.executor.PlayerExecutor;

@Command({"hub", "lobby", "l"})
@CommandDescription("Sends you to a fallback")
@CommandExecutionScope(CommandScope.INGAME_HOSTED_ON_CLOUD_SIDE)
public class HubCommand {


    @Root
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
