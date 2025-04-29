package cloud.hytora.remote.impl.handler;

import cloud.hytora.common.logging.Logger;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.node.packet.NodeCycleDataPacket;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.node.INode;
import cloud.hytora.driver.node.NodeManager;
import cloud.hytora.driver.node.data.INodeData;

public class RemoteNodeUpdateHandler implements PacketHandler<NodeCycleDataPacket> {

    @Override
    public void handle(PacketChannel wrapper, NodeCycleDataPacket packet) {

        String name = packet.getNodeName();
        INodeData data = packet.getData();
        Logger logger = CloudDriver.getInstance().getLogger();
        NodeManager nodeManager = CloudDriver.getInstance().getNodeManager();

        INode node = nodeManager.getNodeByNameOrNull(name);

        if (node == null) {
            logger.warn("Tried updating non-existent node {}! Data: {}", name, data);
            return;
        }
        node.setLastCycleData(data);
        // TODO: 20.04.2025 update node data
    }
}

