package cloud.hytora.bridge.proxy.bungee.listener;

import cloud.hytora.common.misc.StringUtils;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.CloudMessages;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.player.packet.PacketCloudPlayer;
import cloud.hytora.driver.services.ServiceManager;
import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.driver.services.task.IServiceTask;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ProxyPlayerServerListener implements Listener {


    @EventHandler
    public void handle(ServerConnectEvent event) {
        ServerInfo target = event.getTarget();
        ProxiedPlayer player = event.getPlayer();

        CloudMessages cloudMessages = CloudMessages.getInstance();

        ServiceManager serviceManager = CloudDriver.getInstance().getServiceManager();
        ICloudService server = serviceManager.getCachedCloudService(target.getName());
        ICloudPlayer cloudPlayer = CloudDriver.getInstance().getPlayerManager().getCachedCloudPlayer(player.getUniqueId());

        if (cloudPlayer == null) {
            player.disconnect(cloudMessages.getPrefix() + " §cAn error occured whilst trying to connect you to " + target.getName() + ": Your UUID is not registered in CloudCache!");
            event.setCancelled(true);
            return;
        }
        if (server == null) {
            player.disconnect(cloudMessages.getPrefix() + " §cAn error occured whilst trying to connect you to " + target.getName() + ": This Service is not registered in CloudCache!");
            event.setCancelled(true);
            return;
        }
        IServiceTask task = server.getTask();

        if (task == null) {
            player.disconnect(cloudMessages.getPrefix() + " §cAn error occured whilst trying to connect you to " + target.getName() + ": This Task is not registered in CloudCache!");
            event.setCancelled(true);
            return;
        }

        boolean hasTaskPermission = cloudPlayer.hasPermission(task.getPermission());

        if ((task.getPermission() != null && !task.getPermission().trim().isEmpty() && !hasTaskPermission)) {
            //kick player
            event.setCancelled(true);
            player.sendMessage(StringUtils.formatMessage(cloudMessages.getPrefix() + " " + cloudMessages.getTaskHasPermissionMessage(), server.getTask().getPermission()));
        }
        event.setCancelled(false);

        PacketCloudPlayer.forServerConnected(cloudPlayer.getUniqueId(), server.getName()).publish();
    }

    @EventHandler
    public void handle(ServerConnectedEvent event) {
        Server server = event.getServer();
        ProxiedPlayer player = event.getPlayer();

        PacketCloudPlayer.forServerConnectedSuccess(player.getUniqueId(), server.getInfo().getName()).publish();
    }

}
