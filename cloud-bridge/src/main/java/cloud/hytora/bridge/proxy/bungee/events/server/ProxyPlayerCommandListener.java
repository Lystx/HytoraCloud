package cloud.hytora.bridge.proxy.bungee.events.server;

import cloud.hytora.document.Bundle;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.commands.ICommandManager;
import cloud.hytora.driver.commands.context.defaults.PlayerCommandContext;
import cloud.hytora.driver.commands.data.DriverCommand;
import cloud.hytora.driver.commands.data.ProtocolCommandInfo;
import cloud.hytora.driver.commands.data.enums.CommandScope;
import cloud.hytora.driver.common.CloudMessages;
import cloud.hytora.driver.player.ICloudPlayerManager;
import cloud.hytora.driver.player.packet.CloudPlayerExecuteCommandPacket;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.storage.INetworkDocumentStorage;
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
            ICloudPlayer cloudPlayer = CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudPlayerManager.class).getCloudPlayerByUniqueIdOrNull(proxiedPlayer.getUniqueId());

            if (cloudPlayer == null) {
                proxiedPlayer.disconnect(CloudMessages.getInstance().getPrefix() + " §cCouldn't find your CloudPlayer. Please rejoin!");
                return;
            }


            INetworkDocumentStorage storage = CloudDriver.getInstance().getProviderRegistry().getUnchecked(INetworkDocumentStorage.class);
            Bundle bundle = storage.getBundle("ingameCommands");
            List<ProtocolCommandInfo> commands = bundle.toList(ProtocolCommandInfo.class);

            String commandLine = event.getMessage().replace("/", "");
            String finalCommandLine = commandLine;
            ProtocolCommandInfo protocolCommandInfo = commands.stream().filter(c -> finalCommandLine.startsWith(c.getPath())).findFirst().orElse(null);
            if (protocolCommandInfo != null) {
                if (commandLine.startsWith("cloud ")) {
                    commandLine = commandLine.replace("cloud ", "");
                }
                //commandLine = commandLine.replace(commandObject.getPath(), "");
                event.setCancelled(true);
                if (protocolCommandInfo.getScope() == CommandScope.INGAME) {
                    CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICommandManager.class).executeCommand(commandLine.split(" "), new PlayerCommandContext(cloudPlayer));
                } else if (protocolCommandInfo.getScope() == CommandScope.CONSOLE_AND_INGAME | protocolCommandInfo.getScope() == CommandScope.INGAME_HOSTED_ON_CLOUD_SIDE) {

                    CloudPlayerExecuteCommandPacket packet = new CloudPlayerExecuteCommandPacket(cloudPlayer.getUniqueId(), commandLine);
                    packet.publishAsync(); //executing packet to send to cloud
                }
            } else {
                String command = event.getMessage().substring(1).split(" ")[0];
                DriverCommand registeredCommand = CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICommandManager.class).getRootCommands().stream().filter(c -> c.getNames().stream().anyMatch(s -> s.equalsIgnoreCase(command))).findFirst().orElse(null);
                if (registeredCommand != null) {
                    event.setCancelled(true);
                    CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICommandManager.class).executeCommand(commandLine.split(" "), new PlayerCommandContext(cloudPlayer));
                }
            }

        }
    }


}
