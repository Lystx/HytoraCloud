package cloud.hytora.modules.hubcommand.command;

import cloud.hytora.driver.commands.context.defaults.PlayerCommandContext;
import cloud.hytora.driver.commands.data.Command;
import cloud.hytora.driver.commands.data.enums.AllowedCommandSender;
import cloud.hytora.driver.commands.data.enums.CommandScope;
import cloud.hytora.driver.commands.parameter.CommandArguments;
import cloud.hytora.driver.commands.sender.PlayerCommandSender;
import cloud.hytora.driver.common.CloudMessages;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.player.executor.PlayerExecutor;

public class HubCommand {

    @Command(
            label = "hub",
            aliases = {"l", "hlobby", "lobby"},
            scope = CommandScope.INGAME_HOSTED_ON_CLOUD_SIDE
    )
    public void lobbyCommand(PlayerCommandContext ctx, CommandArguments args) {
        ICloudPlayer player = ctx.getPlayer();
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
