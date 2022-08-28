package cloud.hytora.bridge.universal;

import cloud.hytora.bridge.CloudBridge;
import cloud.hytora.driver.networking.packets.DriverUpdatePacket;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.remote.adapter.IBridgeProxyExtension;

public class UniversalBridgeUpdateHandler implements PacketHandler<DriverUpdatePacket> {

    @Override
    public void handle(PacketChannel wrapper, DriverUpdatePacket packet) {
        IBridgeProxyExtension proxy = CloudBridge.getRemoteExtension().asProxyExtension();

        if (proxy == null) {
            return; //no proxy environment
        }

        proxy.clearServices();
        for (ICloudServer allCachedService : packet.getAllCachedServices()) {
            proxy.registerService(allCachedService);
        }

    }
}
