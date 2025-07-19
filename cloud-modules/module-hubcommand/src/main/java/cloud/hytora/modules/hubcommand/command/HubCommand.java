package cloud.hytora.modules.hubcommand.command;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.command.annotation.Command;
import cloud.hytora.driver.command.sender.PlayerCommandSender;
import cloud.hytora.driver.entity.player.CloudPlayer;

import cloud.hytora.driver.entity.services.CloudService;

import static cloud.hytora.driver.command.CommandScope.INGAME_HOSTED_ON_CLOUD_SIDE;

@Command(
        value = {"hub", "lobby", "l"},
        description = "Sends you to a fallback",
        executionScope = INGAME_HOSTED_ON_CLOUD_SIDE
)
public class HubCommand {


    @Command.Root
    public void execute(PlayerCommandSender sender) {
        CloudPlayer player = sender.getPlayer();

        CloudService service = player.getServer();
        if (service == null) {
            player.sendMessage("§cCouldn't send you to a fallback!");
            return;
        }

        if (service.isRegisteredAsFallback()) {
            player.sendMessage(CloudDriver.getInstance().getConfigManager().getConfig().getMessages().getAlreadyOnFallbackMessage());
            return;
        }
        player.asProxyPlayer().sendToFallback();
    }
}
