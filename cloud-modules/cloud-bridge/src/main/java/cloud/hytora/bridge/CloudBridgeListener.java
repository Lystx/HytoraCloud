package cloud.hytora.bridge;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.PublishingType;
import cloud.hytora.driver.event.EventListener;
import cloud.hytora.driver.event.EventOrder;
import cloud.hytora.driver.event.defaults.player.CloudPlayerChangeServerEvent;
import cloud.hytora.driver.event.defaults.player.CloudPlayerDisconnectEvent;
import cloud.hytora.driver.event.defaults.player.CloudPlayerLoginEvent;
import cloud.hytora.driver.event.defaults.player.CloudPlayerUpdateEvent;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.player.PlayerManager;
import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.remote.impl.RemotePlayerManager;

public class CloudBridgeListener {


    private final PlayerManager pm;

    public CloudBridgeListener() {
        this.pm = CloudDriver.getInstance().getPlayerManager();
    }

    @EventListener(order = EventOrder.FIRST)
    public void handleDisconnect(CloudPlayerDisconnectEvent event) {
        ICloudPlayer cloudPlayer = event.getCloudPlayer();

        ((RemotePlayerManager)pm).unregister(cloudPlayer.getUniqueId());
    }

    @EventListener(order = EventOrder.FIRST)
    public void handleUpdate(CloudPlayerUpdateEvent event) {
        ICloudPlayer cloudPlayer = event.getCloudPlayer();

        ((RemotePlayerManager)pm).updateCloudPlayer(cloudPlayer, PublishingType.INTERNAL);
    }


    @EventListener(order = EventOrder.FIRST)
    public void handleJoin(CloudPlayerLoginEvent event) {
        ICloudPlayer cloudPlayer = event.getCloudPlayer();
        ((RemotePlayerManager)pm).registerPlayer(cloudPlayer);
    }


    @EventListener(order = EventOrder.FIRST)
    public void handleChange(CloudPlayerChangeServerEvent event) {
        ICloudPlayer player = event.getPlayer();
        ICloudService server = event.getServer();

        player.setServer(server);
        player.update(PublishingType.INTERNAL);
    }
}
