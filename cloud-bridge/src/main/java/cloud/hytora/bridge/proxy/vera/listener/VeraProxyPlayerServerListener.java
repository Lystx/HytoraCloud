package cloud.hytora.bridge.proxy.vera.listener;

import cloud.hytora.common.misc.StringUtils;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.CloudMessages;
import cloud.hytora.driver.event.defaults.player.CloudPlayerChangeServerEvent;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.player.PlayerManager;
import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.driver.services.ServiceManager;
import cloud.hytora.driver.services.task.IServiceTask;
import cloud.hytora.remote.Remote;
import de.verasoftware.proxy.api.component.ChatComponent;
import de.verasoftware.proxy.api.environment.entity.player.Player;
import de.verasoftware.proxy.api.event.annotation.Listener;
import de.verasoftware.proxy.api.event.defaults.player.PlayerServiceConnectEvent;
import de.verasoftware.proxy.api.event.defaults.player.PlayerServiceSelectedEvent;
import de.verasoftware.proxy.api.server.ProxyServer;


public class VeraProxyPlayerServerListener  {

    private final PlayerManager playerManager;

    public VeraProxyPlayerServerListener() {
        this.playerManager = CloudDriver.getInstance().getPlayerManager();
    }



    @Listener
    public void handle(PlayerServiceConnectEvent event) {
        ProxyServer target = event.getServer();
        Player player = event.getPlayer();

        CloudMessages cloudMessages = CloudMessages.getInstance();

        ServiceManager serviceManager = CloudDriver.getInstance().getServiceManager();
        ICloudService server = serviceManager.getServiceByNameOrNull(target.getName());
        ICloudPlayer cloudPlayer = CloudDriver.getInstance().getPlayerManager().getCachedCloudPlayer(player.getUniqueId());

        if (server == null) {
            player.disconnect(ChatComponent.text(cloudMessages.getPrefix() + " §cAn error occured whilst trying to connect you to " + target.getName() + ": This Service is not registered in CloudCache!"));
            event.setCancelled(true);
            return;
        }
        IServiceTask task = server.getTask();

        if (task == null) {
            player.disconnect(ChatComponent.text(cloudMessages.getPrefix() + " §cAn error occured whilst trying to connect you to " + target.getName() + ": This Task is not registered in CloudCache!"));
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

    @Listener
    public void handle(PlayerServiceSelectedEvent event) {
        ProxyServer server = event.getPlayer().getConnectedServer();
        Player player = event.getPlayer();

        ICloudPlayer cloudPlayer = this.playerManager.getCachedCloudPlayer(player.getUniqueId());
        if (cloudPlayer == null || server == null) {
            return;
        }

        //setting new service
        ICloudService service = CloudDriver.getInstance().getServiceManager().getServiceByNameOrNull(server.getName());
        if (service != null) {
            cloudPlayer.setServer(service);
            cloudPlayer.setProxyServer(Remote.getInstance().thisService());
            cloudPlayer.update();
            CloudDriver.getInstance().getEventManager().callEventGlobally(new CloudPlayerChangeServerEvent(cloudPlayer, service));
        }

    }


}
