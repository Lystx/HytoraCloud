package cloud.hytora.remote.impl.handler;

import cloud.hytora.driver.networking.packets.cache.PacketDriverCacheUpdate;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.remote.Remote;
import cloud.hytora.remote.adapter.proxy.RemoteProxyAdapter;

public class RemoteCacheUpdateHandler implements PacketHandler<PacketDriverCacheUpdate> {

    @Override
    public void handle(PacketChannel channel, PacketDriverCacheUpdate packet) {


        RemoteProxyAdapter proxy = Remote.getInstance().getProxyAdapterOrNull();

        if (proxy == null) {
            return; //no proxy environment
        }

        proxy.clearServices();
        for (CloudService allCachedService : packet.getAllCachedServices()) {
            proxy.registerService(allCachedService);
        }

    }
}
