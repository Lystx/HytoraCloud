package cloud.hytora.node.impl.handler.packet.remote;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.node.packet.NodeRequestServerStartPacket;
import cloud.hytora.driver.networking.protocol.packets.NetworkResponseState;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.driver.services.ICloudServiceManager;

public class NodeRemoteServerStartHandler implements PacketHandler<NodeRequestServerStartPacket> {

    @Override
    public void handle(PacketChannel wrapper, NodeRequestServerStartPacket packet) {
        ICloudServer server = packet.getServer();
        CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudServiceManager.class).startService(server)
                .onTaskSucess(cloudServer -> {
                    if (packet.isDemandsResponse()) {
                        wrapper.prepareResponse().state(NetworkResponseState.OK).execute(packet);
                    }
                })
                .onTaskFailed(throwable -> {
                    if (packet.isDemandsResponse()) {
                        wrapper.prepareResponse().error(throwable).state(NetworkResponseState.FAILED).execute(packet);
                    }
                });
    }
}
