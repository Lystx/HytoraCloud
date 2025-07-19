package cloud.hytora.bridge.proxy.bungee.listener;

import cloud.hytora.common.VersionInfo;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.command.DefaultCommandManager;
import cloud.hytora.driver.command.DriverCommandInfo;
import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.command.annotation.data.RegisteredCommand;
import cloud.hytora.driver.event.defaults.player.CloudEventPlayerCommand;
import cloud.hytora.driver.entity.player.CloudPlayer;
import cloud.hytora.driver.networking.packets.entities.PacketCloudEntityPlayer;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ProxyPlayerCommandListener implements Listener {


    @EventHandler
    public void handleCommand(ChatEvent event) {
        if (!(event.getSender() instanceof ProxiedPlayer)) {
            return;
        }
        String trim = event.getMessage().trim();
        if (trim.equalsIgnoreCase("#hytora")
            || trim.equalsIgnoreCase("#hytoracloud")
            || trim.equalsIgnoreCase("#hc")
            || trim.equalsIgnoreCase("#cloud")
        ) {
            ProxiedPlayer proxiedPlayer = (ProxiedPlayer)event.getSender();
            CloudPlayer cloudPlayer = CloudDriver.getInstance().getPlayerManager().getCachedCloudPlayer(proxiedPlayer.getUniqueId());


            VersionInfo versionInfo = VersionInfo.getCurrentVersion();

            cloudPlayer.sendMessage("§8");
            cloudPlayer.sendMessage("§7This Network is powered by %1Hytora%2Cloud§8:");
            cloudPlayer.sendMessage("§8» §7Version§8: %2" + versionInfo.getName() + "-" + versionInfo.getType().name() + "-" + versionInfo.getVersion());
            cloudPlayer.sendMessage("§8» §7NetworkId§8: %2" + CloudDriver.getInstance().getConfigManager().getConfig().getUniqueNetworkId());
            cloudPlayer.sendMessage("§8» §7Developer§8: %2" + "Lystx");
            cloudPlayer.sendMessage("§8");
            event.setCancelled(true);
            return;
        }
        if (trim.equalsIgnoreCase("/bungee")) {
            ProxiedPlayer proxiedPlayer = (ProxiedPlayer)event.getSender();
            CloudPlayer cloudPlayer = CloudDriver.getInstance().getPlayerManager().getCachedCloudPlayer(proxiedPlayer.getUniqueId());

            cloudPlayer.sendMessage("§8");
            cloudPlayer.sendMessage("§7This Proxy is powered by %1Bungee%2Cord§8:");
            cloudPlayer.sendMessage("§8» §7Version§8: %2" + ProxyServer.getInstance().getVersion());
            cloudPlayer.sendMessage("§8» §7Developer§8: %2" + "md5");
            cloudPlayer.sendMessage("§8");
            event.setCancelled(true);
            return;

        }
        if (event.isCommand() || event.isProxyCommand()) {

            ProxiedPlayer proxiedPlayer = (ProxiedPlayer)event.getSender();
            CloudPlayer cloudPlayer = CloudDriver.getInstance().getPlayerManager().getCachedCloudPlayer(proxiedPlayer.getUniqueId());

            if (cloudPlayer == null) {
                return;
            }

            CloudEventPlayerCommand commandEvent = CloudDriver.getInstance().getEventManager().callEvent(new CloudEventPlayerCommand(cloudPlayer, event.getMessage(), false));

            if (commandEvent.isCancelled()) {
                event.setCancelled(true);
                return;
            }


            List<DriverCommandInfo> filteredCommands = ((DefaultCommandManager) CloudDriver.getInstance()
                    .getCommandManager())
                    .getAllCommands()
                    .stream()
                    .filter(c -> c.getScope().isIngame() && c.getScope() != CommandScope.CONSOLE_AND_INGAME)
                    .collect(Collectors.toList());

            String commandLine = event.getMessage().replace("/", "");

            if (commandLine.trim().equalsIgnoreCase("cloud")) {
                commandLine = "cloud help";
            }
            if (commandLine.startsWith("cloud ")) {
                commandLine = commandLine.replace("cloud ", "");
            } else if (commandLine.startsWith("cloud")) {
                commandLine = commandLine.replace("cloud", "");
            }

            String finalCommandLine = commandLine;
            DriverCommandInfo commandInfo = filteredCommands.stream().filter(c -> finalCommandLine.startsWith(c.getPath())).findFirst().orElse(null);
            if (commandInfo != null) {
                event.setCancelled(true);
                if (commandInfo.getScope() == CommandScope.INGAME_HOSTED_ON_CLOUD_SIDE) {
                    PacketCloudEntityPlayer.forPlayerCommandExecute(cloudPlayer.getUniqueId(), commandLine).publish();
                    return;
                }
                CloudDriver.getInstance().getCommandManager().executeCommand(cloudPlayer, commandLine);
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
