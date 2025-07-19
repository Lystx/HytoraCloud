package cloud.hytora.node.remote.handler;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.entity.node.INode;
import cloud.hytora.driver.entity.node.UniversalNode;
import cloud.hytora.driver.entity.node.data.DefaultNodeData;
import cloud.hytora.driver.networking.packets.entities.PacketCloudEntityNode;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.node.NodeDriver;

public class NodeRemoteHandler implements PacketHandler<PacketCloudEntityNode> {


    @Override
    public void handle(PacketChannel channel, PacketCloudEntityNode packet) {
        PacketBuffer buffer = packet.buffer();

        switch (packet.getPayLoad()) {
            case DATA_REQUEST:
                channel.sendResponse()
                        .setBuffer(buf -> buf.writeObject(
                                CloudDriver.getInstance().getConfigManager().universal().getNodeConfig()
                        ).writeObject(DefaultNodeData.current()))
                        .execute(packet);
                break;
            case DATA_RESPONSE:
                String node = buffer.readString();
                PacketCloudEntityNode.ResponsePayLoad responsePayLoad = buffer.readEnum(PacketCloudEntityNode.ResponsePayLoad.class);
                INode nodeInfo = buffer.readObject(UniversalNode.class);

                switch (responsePayLoad) {
                    case SUCCESS:
                        NodeDriver.getInstance().getNodeManager().registerNode(nodeInfo); //registering node that we connected to
                        CloudDriver.getInstance().getLogger().info("This Node §asuccessfully §7connected to §b{}§8.", nodeInfo);
                        break;
                    case WRONG_AUTH_KEY:
                        CloudDriver.getInstance().getLogger().error("You provided a wrong AuthKey for the Node to check! Check again and reboot the CloudSystem!");
                        break;
                    case ALREADY_NODE_EXISTS:
                        CloudDriver.getInstance().getLogger().error("There is already a Node with the name §e{}", node);
                        break;
                    case SAME_NAME_AS_HEAD_NODE:
                        CloudDriver.getInstance().getLogger().error("You can not name this Node like the HeadNode!");
                        break;
                }
                break;
        }
    }
}
