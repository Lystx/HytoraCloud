package cloud.hytora.node.impl.player;

import cloud.hytora.driver.event.EventManager;
import cloud.hytora.driver.networking.packets.player.CloudPlayerUpdatePacket;
import cloud.hytora.driver.player.CloudPlayer;
import cloud.hytora.driver.player.impl.DefaultPlayerManager;
import cloud.hytora.driver.player.impl.DefaultCloudPlayer;
import cloud.hytora.node.NodeDriver;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class NodePlayerManager extends DefaultPlayerManager {

    public NodePlayerManager(EventManager eventManager) {
        super(eventManager);
    }

    @Override
    public void registerCloudPlayer(@NotNull UUID uniqueID, @NotNull String username) {
        this.cachedCloudPlayers.put(uniqueID, new DefaultCloudPlayer(uniqueID, username));
    }

    @Override
    public void unregisterCloudPlayer(@NotNull UUID uuid, @NotNull String name) {
        this.cachedCloudPlayers.remove(uuid);
    }

    @Override
    public void updateCloudPlayer(@NotNull CloudPlayer cloudPlayer) {
        //Update cache of every component
        CloudPlayerUpdatePacket packet = new CloudPlayerUpdatePacket(cloudPlayer);
        NodeDriver.getInstance().getExecutor().sendPacketToAll(packet);
    }
}
