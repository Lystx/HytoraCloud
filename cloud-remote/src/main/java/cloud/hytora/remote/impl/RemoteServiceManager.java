package cloud.hytora.remote.impl;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.packets.RedirectPacket;
import cloud.hytora.driver.networking.packets.services.CloudServerCacheRegisterPacket;
import cloud.hytora.driver.networking.packets.services.CloudServerCacheUnregisterPacket;
import cloud.hytora.driver.networking.packets.services.ServiceShutdownPacket;
import cloud.hytora.driver.networking.packets.services.CloudServerCacheUpdatePacket;
import cloud.hytora.driver.services.CloudServer;
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
        executor.registerPacketHandler((PacketHandler<ServiceShutdownPacket>) (ctx, packet) -> System.exit(0) /*TODO better*/);
        executor.registerPacketHandler((PacketHandler<CloudServerCacheUnregisterPacket>) (ctx, packet) -> this.getAllCachedServices().remove(getServiceByNameOrNull(packet.getService())));
        executor.registerPacketHandler((PacketHandler<CloudServerCacheRegisterPacket>) (ctx, packet) -> this.getAllCachedServices().add(packet.getService()));

        executor.registerPacketHandler((PacketHandler<CloudServerCacheUpdatePacket>) (ctx, packet) -> {
            CloudServer packetService = packet.getService();
            CloudServer service = getServiceByNameOrNull(packetService.getName());
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
    public cloud.hytora.common.wrapper.Wrapper<CloudServer> startService(@NotNull CloudServer service) {
        //TODO SEND PACKET
        return cloud.hytora.common.wrapper.Wrapper.empty();
    }

    @Override
    public void shutdownService(CloudServer service) {
        // TODO: 11.04.2022
    }

    public CloudServer thisService() {
        return this.getAllCachedServices().stream().filter(it -> it.getName().equalsIgnoreCase(this.property.getName())).findAny().orElse(null);
    }

    @Override
    public void updateService(@NotNull CloudServer service) {
        Remote.getInstance().getClient().sendPacket(new CloudServerCacheUpdatePacket(service));
    }

    @Override
    public void sendPacketToService(CloudServer service, Packet packet) {
        if (service.getName().equalsIgnoreCase(Remote.getInstance().thisService().getName())) {
            CloudDriver.getInstance().getExecutor().handlePacket(null, packet);
            return;
        }
        Remote.getInstance().getClient().sendPacket(new RedirectPacket(service.getName(), packet));
    }
}
