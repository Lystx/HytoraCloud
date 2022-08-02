package cloud.hytora.bridge.proxy.bungee.events.server;

import cloud.hytora.document.Bundle;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.command.CommandObject;
import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.networking.packets.player.CloudPlayerExecuteCommandPacket;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.storage.DriverStorage;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.List;

public class ProxyPlayerCommandListener implements Listener {


    @EventHandler
    public void handleCommand(ChatEvent event) {
        if (event.isCommand() || event.isProxyCommand()) {

            ProxiedPlayer proxiedPlayer = (ProxiedPlayer)event.getSender();
            ICloudPlayer cloudPlayer = CloudDriver.getInstance().getPlayerManager().getCloudPlayerByUniqueIdOrNull(proxiedPlayer.getUniqueId());

            if (cloudPlayer == null) {
                proxiedPlayer.sendMessage("ERROR");
                return;
            }


            DriverStorage storage = CloudDriver.getInstance().getStorage();
            Bundle bundle = storage.getBundle("ingameCommands");
            List<CommandObject> commands = bundle.toList(CommandObject.class);

            String commandLine = event.getMessage().replace("/", "");
            String finalCommandLine = commandLine;
            CommandObject commandObject = commands.stream().filter(c -> finalCommandLine.startsWith(c.getPath())).findFirst().orElse(null);
            if (commandObject != null) {
                if (commandLine.startsWith("cloud ")) {
                    commandLine = commandLine.replace("cloud ", "");
                }
                //commandLine = commandLine.replace(commandObject.getPath(), "");
                event.setCancelled(true);
                if (commandObject.getScope() == CommandScope.INGAME) {
                    CloudDriver.getInstance().getCommandManager().executeCommand(cloudPlayer, event.getMessage());
                } else if (commandObject.getScope() == CommandScope.CONSOLE_AND_INGAME | commandObject.getScope() == CommandScope.INGAME_HOSTED_ON_CLOUD_SIDE) {

                    CloudPlayerExecuteCommandPacket packet = new CloudPlayerExecuteCommandPacket(cloudPlayer.getUniqueId(), commandLine);
                    packet.publishAsync(); //executing packet to send to cloud
                }
            }

        }
    }


}
