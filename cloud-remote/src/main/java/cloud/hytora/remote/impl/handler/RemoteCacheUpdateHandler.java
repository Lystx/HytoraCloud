package cloud.hytora.remote.impl.handler;

import cloud.hytora.driver.networking.packets.DriverUpdatePacket;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.ChannelWrapper;
import cloud.hytora.driver.services.CloudServer;
import cloud.hytora.remote.Remote;
import cloud.hytora.remote.adapter.proxy.RemoteProxyAdapter;

public class RemoteCacheUpdateHandler implements PacketHandler<DriverUpdatePacket> {

    @Override
    public void handle(ChannelWrapper wrapper, DriverUpdatePacket packet) {
        RemoteProxyAdapter proxy = Remote.getInstance().getProxyAdapterOrNull();

        if (proxy == null) {
            return; //no proxy environment
        }

        proxy.clearServices();
        for (CloudServer allCachedService : packet.getAllCachedServices()) {
            proxy.registerService(allCachedService);
        }

    }
}
