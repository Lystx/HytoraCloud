package cloud.hytora.remote.impl;

import cloud.hytora.common.task.IPromise;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.event.EventListener;
import cloud.hytora.driver.event.IEventManager;
import cloud.hytora.driver.event.defaults.server.ServiceRegisterEvent;
import cloud.hytora.driver.event.defaults.server.ServiceUnregisterEvent;
import cloud.hytora.driver.event.defaults.server.ServiceUpdateEvent;
import cloud.hytora.driver.networking.protocol.packets.defaults.RedirectPacket;
import cloud.hytora.driver.node.packet.NodeRequestServerStartPacket;
import cloud.hytora.driver.services.packet.ServiceForceShutdownPacket;
import cloud.hytora.driver.services.packet.ServiceRequestShutdownPacket;
import cloud.hytora.driver.networking.protocol.packets.IPacket;
import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.driver.services.impl.DefaultServiceManager;
import cloud.hytora.driver.networking.IHandlerNetworkExecutor;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;

import cloud.hytora.remote.Remote;
import org.jetbrains.annotations.NotNull;

public class RemoteServiceManager extends DefaultServiceManager {

    public RemoteServiceManager() {
        IHandlerNetworkExecutor executor = CloudDriver.getInstance().getNetworkExecutor();
        executor.registerPacketHandler((PacketHandler<ServiceForceShutdownPacket>) (ctx, packet) -> {
            if (packet.getService().equalsIgnoreCase(Remote.getInstance().thisSidesClusterParticipant().getName())) {
                Remote.getInstance().shutdown();
            }
        });
    }

    @EventListener
    public void handleAdd(ServiceRegisterEvent event) {
        ICloudServer server = event.getCloudServer();
        this.registerService(server);
    }

    @EventListener
    public void handleRemove(ServiceUnregisterEvent event) {
        ICloudServer server = this.getService(event.getService());
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
    public IPromise<ICloudServer> startService(@NotNull ICloudServer service) {

        new NodeRequestServerStartPacket(service, false).publishTo(service.getRunningNodeName());
        return IPromise.newInstance(service);
    }

    @Override
    public ICloudServer thisServiceOrNull() {
        return getAllCachedServices().stream().filter(s -> s.getName().equalsIgnoreCase(Remote.getInstance().getProperty().getName())).findFirst().orElse(null);
    }

    @Override
    public IPromise<ICloudServer> thisService() {
        return IPromise.callAsync(() -> getAllCachedServices().stream().filter(s -> s.getName().equalsIgnoreCase(Remote.getInstance().getProperty().getName())).findFirst().orElse(null));
    }

    @Override
    public void shutdownService(ICloudServer service) {
        CloudDriver.getInstance().getNetworkExecutor().sendPacket(new ServiceRequestShutdownPacket(service.getName()));
    }


    @Override
    public void updateService(@NotNull ICloudServer service) {
        this.updateServerInternally(service);
        CloudDriver.getInstance().getProviderRegistry().getUnchecked(IEventManager.class).callEventGlobally(new ServiceUpdateEvent(service));
    }

    @Override
    public void sendPacketToService(ICloudServer service, @NotNull IPacket packet) {
        if (service.getName().equalsIgnoreCase(Remote.getInstance().thisSidesClusterParticipant().getName())) {
            CloudDriver.getInstance().getNetworkExecutor().handlePacket(null, packet);
            return;
        }
        Remote.getInstance().getNetworkExecutor().sendPacket(new RedirectPacket(service.getName(), packet));
    }

}
