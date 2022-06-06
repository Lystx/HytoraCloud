package cloud.hytora.node.impl.handler.packet.normal;

import cloud.hytora.common.logging.Logger;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.packets.node.NodeCycleDataPacket;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.node.Node;
import cloud.hytora.driver.node.NodeCycleData;
import cloud.hytora.driver.node.NodeManager;


public class NodeDataCycleHandler implements PacketHandler<NodeCycleDataPacket> {

	@Override
	public void handle(PacketChannel wrapper, NodeCycleDataPacket packet) {

		String name = packet.getNodeName();
		NodeCycleData data = packet.getData();
		Logger logger = CloudDriver.getInstance().getLogger();
		NodeManager nodeManager = CloudDriver.getInstance().getNodeManager();

		Node node = nodeManager.getNodeByNameOrNull(name);

		if (node == null) {
			logger.warn("Tried updating non-existent node {}! Data: {}", name, data);
			return;
		}
		logger.debug("{} -> {}", node, data);
		node.setLastCycleData(data);
	}
}
