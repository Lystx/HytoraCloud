package cloud.hytora.bridge.universal;

import cloud.hytora.bridge.CloudBridge;
import cloud.hytora.driver.services.packet.ServiceCommandPacket;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.remote.adapter.IBridgeExtension;

public class UniversalBridgeCommandHandler implements PacketHandler<ServiceCommandPacket> {

    @Override
    public void handle(PacketChannel wrapper, ServiceCommandPacket packet) {
        IBridgeExtension extension = CloudBridge.getRemoteExtension();
        if (extension == null) {
            return;
        }
        extension.executeCommand(packet.getCommand());
    }
}
