package cloud.hytora.remote.impl.handler;

import cloud.hytora.driver.networking.packets.DriverUpdatePacket;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.node.INode;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.driver.services.task.IServiceTask;
import cloud.hytora.driver.services.task.bundle.TaskGroup;
import cloud.hytora.remote.Remote;
import cloud.hytora.remote.adapter.proxy.RemoteProxyAdapter;

import java.util.Collection;

public class RemoteCacheUpdateHandler implements PacketHandler<DriverUpdatePacket> {

    @Override
    public void handle(PacketChannel wrapper, DriverUpdatePacket packet) {


        RemoteProxyAdapter proxy = Remote.getInstance().getProxyAdapterOrNull();

        if (proxy == null) {
            return; //no proxy environment
        }

        proxy.clearServices();
        for (ICloudService allCachedService : packet.getAllCachedServices()) {
            proxy.registerService(allCachedService);
        }

    }
}
