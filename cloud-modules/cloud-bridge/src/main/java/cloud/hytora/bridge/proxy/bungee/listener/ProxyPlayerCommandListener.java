package cloud.hytora.bridge.proxy.bungee.listener;

import cloud.hytora.document.Bundle;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.command.DriverCommandInfo;
import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.command.annotation.data.RegisteredCommand;
import cloud.hytora.driver.common.CloudMessages;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.player.packet.PacketCloudPlayer;
import cloud.hytora.driver.storage.DriverStorage;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Arrays;
import java.util.List;

public class ProxyPlayerCommandListener implements Listener {


    @EventHandler
    public void handleCommand(ChatEvent event) {
        if (event.isCommand() || event.isProxyCommand()) {

            ProxiedPlayer proxiedPlayer = (ProxiedPlayer)event.getSender();
            ICloudPlayer cloudPlayer = CloudDriver.getInstance().getPlayerManager().getCachedCloudPlayer(proxiedPlayer.getUniqueId());

            if (cloudPlayer == null) {
                proxiedPlayer.disconnect(CloudMessages.getInstance().getPrefix() + " §cCouldn't find your CloudPlayer. Please rejoin!");
                return;
            }


            DriverStorage storage = CloudDriver.getInstance().getStorage();
            Bundle bundle = storage.getBundle("ingameCommands");
            List<DriverCommandInfo> commands = bundle.toList(DriverCommandInfo.class);

            String commandLine = event.getMessage().replace("/", "");
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
                    PacketCloudPlayer.forPlayerCommandExecute(cloudPlayer.getUniqueId(), commandLine).publishAsync();
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


}
