package cloud.hytora.node.impl.handler.packet.remote;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.packets.DriverUpdatePacket;
import cloud.hytora.driver.networking.protocol.packets.NetworkResponseState;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.node.packet.NodeRequestServerStopPacket;
import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.node.NodeDriver;

public class NodeRemoteCacheHandler implements PacketHandler<DriverUpdatePacket> {

    @Override
    public void handle(PacketChannel wrapper, DriverUpdatePacket packet) {
        CloudDriver.getInstance().getLogger().info(
                "Received Cache: [{} Servers] [{} Tasks] [{} Groups] [{} Players] [{} Nodes]",
                CloudDriver.getInstance().getServiceManager().getAllCachedServices().size(),
                CloudDriver.getInstance().getServiceTaskManager().getAllCachedTasks().size(),
                CloudDriver.getInstance().getServiceTaskManager().getAllTaskGroups().size(),
                CloudDriver.getInstance().getPlayerManager().getAllCachedCloudPlayers().size(),
                CloudDriver.getInstance().getNodeManager().getAllCachedNodes().size()
        );
    }
}
