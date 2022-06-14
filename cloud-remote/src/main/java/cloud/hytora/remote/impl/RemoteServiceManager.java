package cloud.hytora.remote.impl;

import cloud.hytora.common.wrapper.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.event.EventListener;
import cloud.hytora.driver.event.defaults.server.ServiceRegisterEvent;
import cloud.hytora.driver.event.defaults.server.ServiceUnregisterEvent;
import cloud.hytora.driver.event.defaults.server.ServiceUpdateEvent;
import cloud.hytora.driver.networking.packets.RedirectPacket;
import cloud.hytora.driver.networking.packets.services.ServiceForceShutdownPacket;
import cloud.hytora.driver.networking.packets.services.ServiceRequestShutdownPacket;
import cloud.hytora.driver.services.ServiceInfo;
import cloud.hytora.driver.services.impl.DefaultServiceManager;
import cloud.hytora.driver.networking.AdvancedNetworkExecutor;
import cloud.hytora.driver.networking.protocol.packets.Packet;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;

import cloud.hytora.remote.Remote;
import org.jetbrains.annotations.NotNull;

public class RemoteServiceManager extends DefaultServiceManager {


    public RemoteServiceManager() {
        AdvancedNetworkExecutor executor = CloudDriver.getInstance().getExecutor();
        executor.registerPacketHandler((PacketHandler<ServiceForceShutdownPacket>) (ctx, packet) -> {
            if (packet.getService().equalsIgnoreCase(Remote.getInstance().thisService().getName())) {
                Remote.getInstance().shutdown();
            }
        });
    }

    @EventListener
    public void handleAdd(ServiceRegisterEvent event) {
        ServiceInfo server = event.getServiceInfo();
        this.registerService(server);
    }

    @EventListener
    public void handleRemove(ServiceUnregisterEvent event) {
        ServiceInfo server = this.getServiceByNameOrNull(event.getService());
        if (server == null) {
            return;
        }
        this.unregisterService(server);
    }

    @EventListener
    public void handleUpdate(ServiceUpdateEvent event) {
        ServiceInfo server = event.getService();

        this.updateServerInternally(server);
    }

    @Override
    public Task<ServiceInfo> startService(@NotNull ServiceInfo service) {
        //TODO SEND PACKET
        return Task.empty();
    }

    @Override
    public void shutdownService(ServiceInfo service) {
        CloudDriver.getInstance().getExecutor().sendPacket(new ServiceRequestShutdownPacket(service.getName()));
    }


    @Override
    public void updateService(@NotNull ServiceInfo service) {
        this.updateServerInternally(service);
        CloudDriver.getInstance().getEventManager().callEventGlobally(new ServiceUpdateEvent(service));
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
