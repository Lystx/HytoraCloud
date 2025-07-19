package cloud.hytora.bridge.minecraft.command;

import cloud.hytora.common.scheduler.Scheduler;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.command.annotation.Command;
import cloud.hytora.driver.command.sender.PlayerCommandSender;
import cloud.hytora.driver.config.INetworkConfig;
import cloud.hytora.driver.config.def.UniversalCloudMessages;
import cloud.hytora.driver.entity.player.CloudPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static cloud.hytora.driver.command.CommandScope.INGAME_SPIGOT;

@Command(
        value = {"stop", "end", "shutdown", "minecraft:stop"},
        description = "Stops this server",
        executionScope = INGAME_SPIGOT
)
public class StopCommand {

    private final Collection<UUID> confirm;

    public StopCommand() {
        this.confirm = new ArrayList<>();
    }


    @Command.Root
    public void execute(PlayerCommandSender sender) {
        CloudPlayer player = sender.getPlayer();
        INetworkConfig config = CloudDriver.getInstance().getConfigManager().getConfig();
        UniversalCloudMessages messages = config.getMessages();

        if (!player.hasPermission("system.stop")) {
            sender.sendMessage("§cYou are not permitted to perform this action!");
            return;
        }
        if (confirm.stream().anyMatch(id -> id.equals(player.getUniqueId()))) {
            sender.sendMessage("§cStopping server in §e3 seconds§c...");

            CloudDriver.getInstance()
                    .getScheduledExecutor()
                    .schedule(() -> CloudDriver.getInstance().shutdown(), 3, TimeUnit.SECONDS);
            return;
        }
        sender.sendMessage( "§cPlease §econfirm §cyour action by typing this command §eagain §cwithin the next §e10 seconds§c!");
        confirm.add(player.getUniqueId());
        CloudDriver.getInstance()
                .getScheduledExecutor()
                .schedule(() -> confirm.remove(player.getUniqueId()), 10, TimeUnit.SECONDS);
    }
}