package cloud.hytora.bridge.spigot.listener;

import cloud.hytora.document.Bundle;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.commands.ICommandManager;
import cloud.hytora.driver.commands.context.defaults.PlayerCommandContext;
import cloud.hytora.driver.commands.data.DriverCommand;
import cloud.hytora.driver.commands.data.ProtocolCommandInfo;
import cloud.hytora.driver.commands.data.enums.CommandScope;
import cloud.hytora.driver.common.CloudMessages;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.player.ICloudPlayerManager;
import cloud.hytora.driver.player.packet.CloudPlayerExecuteCommandPacket;
import cloud.hytora.driver.storage.INetworkDocumentStorage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;

public class  BukkitPlayerCommandListener implements Listener {


    @EventHandler
    public void handleCommand(PlayerCommandPreprocessEvent event) {
        if (!event.getMessage().startsWith("/")) {
            return;
        }

        Player player = event.getPlayer();
        ICloudPlayer cloudPlayer = CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudPlayerManager.class).getCloudPlayerByUniqueIdOrNull(player.getUniqueId());

        if (cloudPlayer == null) {
            player.kickPlayer(CloudMessages.getInstance().getPrefix() + " §cCouldn't find your CloudPlayer. Please rejoin!");
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
