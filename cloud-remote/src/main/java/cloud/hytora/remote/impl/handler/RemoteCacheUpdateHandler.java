package cloud.hytora.remote.impl.handler;

import cloud.hytora.driver.networking.packets.DriverUpdatePacket;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.remote.Remote;
import cloud.hytora.remote.adapter.RemoteProxyAdapter;

public class RemoteCacheUpdateHandler implements PacketHandler<DriverUpdatePacket> {

    @Override
    public void handle(PacketChannel wrapper, DriverUpdatePacket packet) {
        RemoteProxyAdapter proxy = Remote.getInstance().getProxyAdapterOrNull();

        if (proxy == null) {
            return; //no proxy environment
        }

        proxy.clearServices();
        for (ICloudServer allCachedService : packet.getAllCachedServices()) {
            proxy.registerService(allCachedService);
        }

    }
}
