package cloud.hytora.bridge.proxy.bungee.events.server;

import cloud.hytora.common.misc.StringUtils;
import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.CloudMessages;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.services.ServiceManager;
import cloud.hytora.driver.player.PlayerManager;
import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.driver.services.task.IServiceTask;
import cloud.hytora.remote.Remote;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.stream.Collectors;

public class ProxyPlayerServerListener implements Listener {

    private final PlayerManager playerManager;

    public ProxyPlayerServerListener() {
        this.playerManager = CloudDriver.getInstance().getPlayerManager();
    }

    @EventHandler
    public void handle(ServerConnectEvent event) {
        ServerInfo target = event.getTarget();
        ProxiedPlayer player = event.getPlayer();

        CloudMessages cloudMessages = CloudMessages.getInstance();

        ServiceManager serviceManager = CloudDriver.getInstance().getServiceManager();
        ICloudServer server = serviceManager.getServiceByNameOrNull(target.getName());
        ICloudPlayer cloudPlayer = CloudDriver.getInstance().getPlayerManager().getCloudPlayerByUniqueIdOrNull(player.getUniqueId());

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

        boolean hasTaskPermission = (cloudPlayer == null ? player.hasPermission(task.getPermission()) : cloudPlayer.hasPermission(task.getPermission()));

        if ((task.getPermission() != null && !task.getPermission().trim().isEmpty() && !hasTaskPermission)) {
            //kick player
            event.setCancelled(true);
            player.sendMessage(StringUtils.formatMessage(cloudMessages.getPrefix() + " " + cloudMessages.getTaskHasPermissionMessage(), server.getTask().getPermission()));
        }
        event.setCancelled(false);

    }

    @EventHandler
    public void handle(ServerConnectedEvent event) {
        Server server = event.getServer();
        ProxiedPlayer player = event.getPlayer();

        ICloudPlayer cloudPlayer = this.playerManager.getCloudPlayerByUniqueIdOrNull(player.getUniqueId());
        if (cloudPlayer == null) {
            return;
        }

        //setting new service
        ICloudServer service = CloudDriver.getInstance().getServiceManager().getServiceByNameOrNull(server.getInfo().getName());
        if (service != null) {
            cloudPlayer.setServer(service);
            cloudPlayer.setProxyServer(Remote.getInstance().thisService());
            cloudPlayer.update();
        }
    }

    @EventHandler
    public void handle(ServerKickEvent event) {
        ProxiedPlayer player = event.getPlayer();
        ICloudServer fallback = CloudDriver.getInstance().getServiceManager().getFallbackAsServiceOrNull();
        CloudMessages cloudMessages = CloudMessages.getInstance();

        if (fallback == null) {
            player.disconnect(new TextComponent(cloudMessages.getPrefix() + " " + cloudMessages.getNoAvailableFallbackMessage()));
        } else {
            event.setCancelServer(ProxyServer.getInstance().getServerInfo(fallback.getName()));
        }
    }

}
