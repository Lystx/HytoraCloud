package cloud.hytora.remote.impl.handler;

import cloud.hytora.common.logging.Logger;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.PublishingType;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.entity.node.data.DefaultNodeData;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.entity.node.INode;
import cloud.hytora.driver.entity.node.NodeManager;
import cloud.hytora.driver.entity.node.data.INodeCycleData;
import cloud.hytora.driver.networking.packets.entities.PacketCloudEntityNode;

public class RemoteNodeUpdateHandler implements PacketHandler<PacketCloudEntityNode> {

    @Override
    public void handle(PacketChannel channel, PacketCloudEntityNode packet) {

        PacketBuffer buffer = packet.buffer();

        switch (packet.getPayLoad()) {

            case CYCLE_DATA:

                String name = buffer.readString();
                INodeCycleData data = buffer.readObject(DefaultNodeData.class);
                Logger logger = CloudDriver.getInstance().getLogger();
                NodeManager nodeManager = CloudDriver.getInstance().getNodeManager();

                INode node = nodeManager.getCachedNode(name);

                if (node == null) {
                    logger.warn("Tried updating non-existent node {}! Data: {}", name, data);
                    return;
                }
                node.setLastCycleData(data);
                node.update(PublishingType.GLOBAL);
                break;
        }

    }
}

