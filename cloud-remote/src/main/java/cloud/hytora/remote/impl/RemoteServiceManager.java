package cloud.hytora.remote.impl;

import cloud.hytora.common.wrapper.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.packets.RedirectPacket;
import cloud.hytora.driver.networking.packets.services.CloudServerCacheRegisterPacket;
import cloud.hytora.driver.networking.packets.services.CloudServerCacheUnregisterPacket;
import cloud.hytora.driver.networking.packets.services.ServiceShutdownPacket;
import cloud.hytora.driver.networking.packets.services.CloudServerCacheUpdatePacket;
import cloud.hytora.driver.services.ServiceInfo;
import cloud.hytora.driver.services.impl.DefaultServiceManager;
import cloud.hytora.driver.networking.AdvancedNetworkExecutor;
import cloud.hytora.driver.networking.protocol.packets.Packet;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;

import cloud.hytora.driver.services.utils.RemoteIdentity;
import cloud.hytora.remote.Remote;
import org.jetbrains.annotations.NotNull;

public class RemoteServiceManager extends DefaultServiceManager {

    private final RemoteIdentity property;

    public RemoteServiceManager(RemoteIdentity property) {
        this.property = property;
        AdvancedNetworkExecutor executor = CloudDriver.getInstance().getExecutor();
        executor.registerPacketHandler((PacketHandler<ServiceShutdownPacket>) (ctx, packet) -> Remote.getInstance().shutdown());
        executor.registerPacketHandler((PacketHandler<CloudServerCacheUnregisterPacket>) (ctx, packet) -> this.getAllCachedServices().remove(getServiceByNameOrNull(packet.getService())));
        executor.registerPacketHandler((PacketHandler<CloudServerCacheRegisterPacket>) (ctx, packet) -> this.getAllCachedServices().add(packet.getService()));

        executor.registerPacketHandler((PacketHandler<CloudServerCacheUpdatePacket>) (ctx, packet) -> {
            ServiceInfo packetService = packet.getService();
            ServiceInfo service = getServiceByNameOrNull(packetService.getName());
            if (service == null) {
                return;
            }

            service.cloneInternally(packetService, service);

            if (allCachedServices.removeIf(s -> s.getName().equalsIgnoreCase(service.getName()))) {
                allCachedServices.add(service);
            }
        });
    }

    @Override
    public Task<ServiceInfo> startService(@NotNull ServiceInfo service) {
        //TODO SEND PACKET
        return Task.empty();
    }

    @Override
    public void shutdownService(ServiceInfo service) {
        // TODO: 11.04.2022
    }

    public ServiceInfo thisService() {
        return this.getAllCachedServices().stream().filter(it -> it.getName().equalsIgnoreCase(this.property.getName())).findAny().orElse(null);
    }

    @Override
    public void updateService(@NotNull ServiceInfo service) {
        Remote.getInstance().getClient().sendPacket(new CloudServerCacheUpdatePacket(service));
    }

    @Override
    public void sendPacketToService(ServiceInfo service, Packet packet) {
        if (service.getName().equalsIgnoreCase(Remote.getInstance().thisService().getName())) {
            CloudDriver.getInstance().getExecutor().handlePacket(null, packet);
            return;
        }
        Remote.getInstance().getClient().sendPacket(new RedirectPacket(service.getName(), packet));
    }
}
