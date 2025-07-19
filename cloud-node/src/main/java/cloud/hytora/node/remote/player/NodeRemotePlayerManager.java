package cloud.hytora.node.remote.player;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.entity.player.CloudPlayer;
import cloud.hytora.driver.entity.player.PlayerExtension;
import cloud.hytora.driver.entity.player.extension.CloudBukkitPlayer;
import cloud.hytora.driver.entity.player.extension.CloudProxyPlayer;
import cloud.hytora.driver.event.EventManager;
import cloud.hytora.remote.impl.extension.RemoteBukkitPlayer;
import cloud.hytora.remote.impl.extension.RemoteProxyPlayer;
import cloud.hytora.remote.impl.RemotePlayerManager;

public class NodeRemotePlayerManager extends RemotePlayerManager implements PlayerExtension {

    public NodeRemotePlayerManager(EventManager eventManager) {
        super(eventManager);

        CloudDriver.getInstance().setProvider(PlayerExtension.class, this);
    }

    @Override
    public CloudProxyPlayer createProxyPlayer(CloudPlayer cloudPlayer) {
        return new RemoteProxyPlayer(cloudPlayer);
    }

    @Override
    public CloudBukkitPlayer createBukkitPlayer(CloudPlayer cloudPlayer) {
        return new RemoteBukkitPlayer(cloudPlayer);
    }
}
