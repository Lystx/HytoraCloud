package cloud.hytora.node.impl.handler.packet.normal;

import cloud.hytora.common.logging.Logger;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.node.packet.NodeCycleDataPacket;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.node.INode;
import cloud.hytora.driver.node.NodeManager;
import cloud.hytora.driver.node.data.INodeData;


public class NodeDataCycleHandler implements PacketHandler<NodeCycleDataPacket> {

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
		logger.trace("Updated Node {} => {}", node.getName(), data);
		node.setLastCycleData(data);
	}
}
