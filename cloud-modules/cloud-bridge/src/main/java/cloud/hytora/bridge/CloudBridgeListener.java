package cloud.hytora.bridge;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.PublishingType;
import cloud.hytora.driver.event.listener.EventListener;
import cloud.hytora.driver.event.type.EventOrder;
import cloud.hytora.driver.event.defaults.player.CloudEventPlayerChangeServer;
import cloud.hytora.driver.event.defaults.player.CloudEventPlayerDisconnect;
import cloud.hytora.driver.event.defaults.player.CloudEventPlayerLoginSuccess;
import cloud.hytora.driver.event.defaults.player.CloudEventPlayerUpdate;
import cloud.hytora.driver.entity.player.CloudPlayer;
import cloud.hytora.driver.entity.player.PlayerManager;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.remote.impl.RemotePlayerManager;

public class CloudBridgeListener {


    private final PlayerManager pm;

    public CloudBridgeListener() {
        this.pm = CloudDriver.getInstance().getPlayerManager();

    }

    @EventListener(order = EventOrder.FIRST)
    public void handleDisconnect(CloudEventPlayerDisconnect event) {
        CloudPlayer cloudPlayer = event.getCloudPlayer();

        ((RemotePlayerManager)pm).unregister(cloudPlayer.getUniqueId());
        CloudDriver.getInstance().getLogger().debug("Disconnect[name={}, uuid={}]", cloudPlayer.getName(), cloudPlayer.getUniqueId());
    }



    @EventListener(order = EventOrder.FIRST)
    public void handleUpdate(CloudEventPlayerUpdate event) {
        CloudPlayer cloudPlayer = event.getCloudPlayer();

        pm.updateCloudPlayer(cloudPlayer, PublishingType.INTERNAL);
        CloudDriver.getInstance().getLogger().debug("Updated[name={}, uuid={}]", cloudPlayer.getName(), cloudPlayer.getUniqueId());

    }


    @EventListener(order = EventOrder.FIRST)
    public void handleJoin(CloudEventPlayerLoginSuccess event) {
        CloudPlayer cloudPlayer = event.getCloudPlayer();
        ((RemotePlayerManager)pm).registerPlayer(cloudPlayer);
        CloudDriver.getInstance().getLogger().debug("Login[name={}, uuid={}]", cloudPlayer.getName(), cloudPlayer.getUniqueId());

    }


    @EventListener(order = EventOrder.FIRST)
    public void handleChange(CloudEventPlayerChangeServer event) {
        CloudPlayer cloudPlayer = event.getPlayer();
        CloudService server = event.getServer();

        cloudPlayer.setServer(server);
        cloudPlayer.update(PublishingType.INTERNAL);

        CloudDriver.getInstance().getLogger().debug("ServerChange[name={}, uuid={}]", cloudPlayer.getName(), cloudPlayer.getUniqueId());

    }
}
