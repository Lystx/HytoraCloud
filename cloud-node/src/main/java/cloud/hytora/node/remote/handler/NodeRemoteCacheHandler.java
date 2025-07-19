package cloud.hytora.node.remote.handler;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.packets.cache.PacketDriverCacheUpdate;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;

public class NodeRemoteCacheHandler implements PacketHandler<PacketDriverCacheUpdate> {

    @Override
    public void handle(PacketChannel channel, PacketDriverCacheUpdate packet) {
        CloudDriver.getInstance().getLogger().debug(
                "Received Cache: [{} Servers] [{} Tasks] [{} Groups] [{} Players] [{} Nodes]",
                CloudDriver.getInstance().getServiceManager().getAllCachedServices().size(),
                CloudDriver.getInstance().getServiceTaskManager().getAllCachedTasks().size(),
                CloudDriver.getInstance().getServiceTaskManager().getAllCachedTaskGroups().size(),
                CloudDriver.getInstance().getPlayerManager().getAllCachedCloudPlayers().size(),
                CloudDriver.getInstance().getNodeManager().getAllCachedNodes().size()
        );
    }
}
