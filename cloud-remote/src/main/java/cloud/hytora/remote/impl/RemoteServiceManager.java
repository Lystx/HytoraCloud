package cloud.hytora.remote.impl;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.event.EventListener;
import cloud.hytora.driver.event.defaults.server.ServiceRegisterEvent;
import cloud.hytora.driver.event.defaults.server.ServiceUnregisterEvent;
import cloud.hytora.driver.event.defaults.server.ServiceUpdateEvent;
import cloud.hytora.driver.networking.packets.RedirectPacket;
import cloud.hytora.driver.node.packet.NodeRequestServerStartPacket;
import cloud.hytora.driver.services.packet.ServiceForceShutdownPacket;
import cloud.hytora.driver.services.packet.ServiceRequestShutdownPacket;
import cloud.hytora.driver.networking.protocol.packets.IPacket;
import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.driver.services.impl.DefaultServiceManager;
import cloud.hytora.driver.networking.AdvancedNetworkExecutor;
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
        ICloudServer server = event.getICloudServer();
        this.registerService(server);
    }

    @EventListener
    public void handleRemove(ServiceUnregisterEvent event) {
        ICloudServer server = this.getServiceByNameOrNull(event.getService());
        if (server == null) {
            return;
        }
        this.unregisterService(server);
    }

    @EventListener
    public void handleUpdate(ServiceUpdateEvent event) {
        ICloudServer server = event.getService();

        this.updateServerInternally(server);
    }


    @Override
    public Task<ICloudServer> startService(@NotNull ICloudServer service) {

        new NodeRequestServerStartPacket(service, false).publishTo(service.getRunningNodeName());
        return Task.build(service);
    }

    @Override
    public Task<ICloudServer> thisService() {
        return Task.build(getAllCachedServices().stream().filter(s -> s.getName().equalsIgnoreCase(Remote.getInstance().getProperty().getName())).findFirst().orElse(null));
    }

    @Override
    public void shutdownService(ICloudServer service) {
        CloudDriver.getInstance().getExecutor().sendPacket(new ServiceRequestShutdownPacket(service.getName()));
    }


    @Override
    public void updateService(@NotNull ICloudServer service) {
        this.updateServerInternally(service);
        CloudDriver.getInstance().getEventManager().callEventGlobally(new ServiceUpdateEvent(service));
    }

    @Override
    public void sendPacketToService(ICloudServer service, IPacket packet) {
        if (service.getName().equalsIgnoreCase(Remote.getInstance().thisService().getName())) {
            CloudDriver.getInstance().getExecutor().handlePacket(null, packet);
            return;
        }
        Remote.getInstance().getClient().sendPacket(new RedirectPacket(service.getName(), packet));
    }

}
