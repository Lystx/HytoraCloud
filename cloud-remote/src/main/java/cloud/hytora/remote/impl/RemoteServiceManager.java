package cloud.hytora.remote.impl;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.PublishingType;
import cloud.hytora.driver.event.EventListener;
import cloud.hytora.driver.event.defaults.server.ServiceRegisterEvent;
import cloud.hytora.driver.event.defaults.server.ServiceUnregisterEvent;
import cloud.hytora.driver.event.defaults.server.ServiceUpdateEvent;
import cloud.hytora.driver.networking.packets.RedirectPacket;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import cloud.hytora.driver.networking.protocol.packets.NetworkResponseState;
import cloud.hytora.driver.node.packet.NodeRequestServerStartPacket;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.services.packet.ServiceForceShutdownPacket;
import cloud.hytora.driver.services.packet.ServiceRequestShutdownPacket;
import cloud.hytora.driver.networking.protocol.packets.IPacket;
import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.driver.services.impl.DefaultServiceManager;
import cloud.hytora.driver.networking.AdvancedNetworkExecutor;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;

import cloud.hytora.driver.services.utils.ServiceState;
import cloud.hytora.driver.services.utils.ServiceVisibility;
import cloud.hytora.remote.Remote;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

public class RemoteServiceManager extends DefaultServiceManager {

    public RemoteServiceManager() {
        AdvancedNetworkExecutor executor = CloudDriver.getInstance().getExecutor();
        executor.registerPacketHandler((PacketHandler<ServiceForceShutdownPacket>) (ctx, packet) -> {
            if (packet.getService().equalsIgnoreCase(Remote.getInstance().thisService().getName())) {

                ICloudService cloudServer = Remote.getInstance().thisService();
                cloudServer.setServiceState(ServiceState.STOPPING);
                cloudServer.setServiceVisibility(ServiceVisibility.INVISIBLE);
                cloudServer.update();
                Remote.getInstance().shutdown();
            }
        });
    }


    @Override
    public ICloudService findFallback(ICloudPlayer player) {
        return CloudDriver.getInstance().getServiceManager().getAllCachedServices().stream()
                .filter(service -> service.getServiceState() == ServiceState.ONLINE)
                .filter(service -> service.getServiceVisibility() == ServiceVisibility.VISIBLE)
                .filter(service -> !service.getTask().getVersion().isProxy())
                .filter(service -> service.getTask().getFallback().isEnabled())
                .filter(service -> (player.getServer() == null || !player.getServer().getName().equals(service.getName())))
                .min(Comparator.comparing(s -> s.getOnlinePlayers().size()))
                .orElse(null);
    }

    @EventListener
    public void handleAdd(ServiceRegisterEvent event) {
        ICloudService server = event.getCloudServer();
        this.registerService(server);
    }

    @EventListener
    public void handleRemove(ServiceUnregisterEvent event) {
        ICloudService server = this.getCachedCloudService(event.getService());
        if (server == null) {
            return;
        }
        this.unregisterService(server);
    }

    @EventListener
    public void handleUpdate(ServiceUpdateEvent event) {
        ICloudService server = event.getService();

        this.updateServerInternally(server);
    }


    @Override
    public Task<ICloudService> startService(@NotNull ICloudService service) {
        Task<ICloudService> task = Task.empty();
        AbstractPacket packet = new NodeRequestServerStartPacket(service, true);

        packet.awaitResponse(service.getRunningNodeName())
                .onTaskSucess(response -> {
                    if (response.state() == NetworkResponseState.OK) {
                        task.setResult(service);
                    } else if (response.state() == NetworkResponseState.FAILED) {
                        task.setFailure(response.error());
                    }

                });
        return task;
    }

    @Override
    public ICloudService thisService() {
        return getAllCachedServices().stream().filter(s -> s.getName().equalsIgnoreCase(Remote.getInstance().getProperty().getName())).findFirst().orElse(null);
    }

    @Override
    public Task<ICloudService> getThisService() {
        return Task.callAsyncNonNull(() -> getAllCachedServices().stream().filter(s -> s.getName().equalsIgnoreCase(Remote.getInstance().getProperty().getName())).findFirst().orElse(null));
    }

    @Override
    public void shutdownService(ICloudService service) {
        CloudDriver.getInstance().getExecutor().sendPacket(new ServiceRequestShutdownPacket(service.getName()));
    }


    @Override
    public void updateService(@NotNull ICloudService service, PublishingType... type) {
        this.updateServerInternally(service);

        PublishingType publishingType = PublishingType.get(type);

        switch (publishingType) {
            case INTERNAL:
                this.updateServerInternally(service);
                break;

            case GLOBAL:
                updateService(service, PublishingType.INTERNAL);
                updateService(service, PublishingType.PROTOCOL);
                break;
            case PROTOCOL:
                //calling update event on every other side
                CloudDriver.getInstance().getEventManager().callEvent(new ServiceUpdateEvent(service), PublishingType.PROTOCOL);

                break;
        }
    }

    @Override
    public void sendPacketToService(ICloudService service, IPacket packet) {
        if (service.getName().equalsIgnoreCase(Remote.getInstance().thisService().getName())) {
            CloudDriver.getInstance().getExecutor().handlePacket(null, packet);
            return;
        }
        Remote.getInstance().getClient().sendPacket(new RedirectPacket(service.getName(), packet));
    }

}
