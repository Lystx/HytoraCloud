package cloud.hytora.node.handler.packet.remote;

import cloud.hytora.common.task.IPromise;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.node.packet.NodeRequestServerStopPacket;
import cloud.hytora.driver.networking.protocol.packets.NetworkResponseState;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.driver.services.ICloudServiceManager;
import cloud.hytora.node.NodeDriver;

public class NodeRemoteServerStopHandler implements PacketHandler<NodeRequestServerStopPacket> {

    @Override
    public void handle(PacketChannel wrapper, NodeRequestServerStopPacket packet) {
        String server = packet.getServerName();
        IPromise<ICloudServer> service = CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudServiceManager.class).getServiceAsync(server);
        service.ifPresent(s -> NodeDriver.getInstance().getNode().stopServer(s));
        if (packet.isDemandsResponse()) {
            wrapper.prepareResponse().state(service.isPresent() ? NetworkResponseState.OK : NetworkResponseState.FAILED).execute(packet);
        }
    }
}
