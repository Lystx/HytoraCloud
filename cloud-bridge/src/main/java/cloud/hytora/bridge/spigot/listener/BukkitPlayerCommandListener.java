package cloud.hytora.bridge.spigot.listener;

import cloud.hytora.document.Bundle;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.command.DriverCommandInfo;
import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.command.annotation.data.RegisteredCommand;
import cloud.hytora.driver.common.CloudMessages;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.player.packet.CloudPlayerExecuteCommandPacket;
import cloud.hytora.driver.storage.DriverStorage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Arrays;
import java.util.List;

public class BukkitPlayerCommandListener implements Listener {


    @EventHandler
    public void handleCommand(PlayerCommandPreprocessEvent event) {
        if (!event.getMessage().startsWith("/")) {
            return;
        }

        Player player = event.getPlayer();
        ICloudPlayer cloudPlayer = CloudDriver.getInstance().getPlayerManager().getCachedCloudPlayer(player.getUniqueId());

        if (cloudPlayer == null) {
            player.kickPlayer(CloudMessages.getInstance().getPrefix() + " §cCouldn't find your CloudPlayer. Please rejoin!");
            return;
        }


        DriverStorage storage = CloudDriver.getInstance().getStorage();
        Bundle bundle = storage.getBundle("ingameCommands");
        List<DriverCommandInfo> commands = bundle.toList(DriverCommandInfo.class);


        String commandLine = event.getMessage().replace("/", "");
        if (commandLine.equalsIgnoreCase("cloud")) {
            commandLine = "cloud help";
        }
        String finalCommandLine = commandLine;
        DriverCommandInfo commandObject = commands.stream().filter(c -> finalCommandLine.startsWith(c.getPath())).findFirst().orElse(null);
        if (commandObject != null) {
            if (commandLine.startsWith("cloud ")) {
                commandLine = commandLine.replace("cloud ", "");
            }
            //commandLine = commandLine.replace(commandObject.getPath(), "");
            event.setCancelled(true);
            if (commandObject.getScope() == CommandScope.INGAME) {
                CloudDriver.getInstance().getCommandManager().executeCommand(cloudPlayer, commandLine);
            } else if (commandObject.getScope() == CommandScope.CONSOLE_AND_INGAME | commandObject.getScope() == CommandScope.INGAME_HOSTED_ON_CLOUD_SIDE) {
                CloudPlayerExecuteCommandPacket packet = new CloudPlayerExecuteCommandPacket(cloudPlayer.getUniqueId(), commandLine);
                packet.publishAsync(); //executing packet to send to cloud
            }
        } else {
            String command = event.getMessage().substring(1).split(" ")[0];
            RegisteredCommand registeredCommand = CloudDriver.getInstance().getCommandManager().getCommands().stream().filter(c -> Arrays.stream(c.getNames()).anyMatch(s -> s.equalsIgnoreCase(command))).findFirst().orElse(null);
            if (registeredCommand != null) {
                event.setCancelled(true);
                CloudDriver.getInstance().getCommandManager().executeCommand(cloudPlayer, commandLine);
            }
        }


    }

}
