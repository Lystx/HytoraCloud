package cloud.hytora.node.impl.handler.packet.normal;

import cloud.hytora.common.logging.Logger;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.packets.DriverRequestCachePacket;
import cloud.hytora.driver.networking.packets.DriverUpdatePacket;
import cloud.hytora.driver.networking.protocol.packets.NetworkResponseState;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.node.INode;
import cloud.hytora.driver.node.NodeManager;
import cloud.hytora.driver.node.data.INodeData;
import cloud.hytora.driver.node.packet.NodeCycleDataPacket;

import java.io.IOException;


public class NodeCacheRequestHandler implements PacketHandler<DriverRequestCachePacket> {

	@Override
	public void handle(PacketChannel wrapper, DriverRequestCachePacket packet) {

		packet.respond(NetworkResponseState.OK, buf -> {
			try {
				buf.writePacket(new DriverUpdatePacket());
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
}
