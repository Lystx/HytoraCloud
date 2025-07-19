package cloud.hytora.remote.impl;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.PublishingType;
import cloud.hytora.driver.common.exception.IncompatibleDriverEnvironmentException;
import cloud.hytora.driver.event.listener.EventListener;
import cloud.hytora.driver.event.defaults.server.CloudEventServiceRegistered;
import cloud.hytora.driver.event.defaults.server.CloudEventServiceUnregistered;
import cloud.hytora.driver.event.defaults.server.CloudEventServiceUpdate;
import cloud.hytora.driver.networking.packets.other.PacketRedirecting;
import cloud.hytora.driver.networking.packets.response.NetworkResponseState;
import cloud.hytora.driver.networking.packets.entities.PacketCloudEntityNode;
import cloud.hytora.driver.entity.player.CloudPlayer;
import cloud.hytora.driver.networking.packets.entities.PacketCloudEntityService;
import cloud.hytora.driver.networking.protocol.packets.IPacket;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.entity.services.impl.DefaultServiceManager;

import cloud.hytora.driver.entity.services.utils.ServiceState;
import cloud.hytora.driver.entity.services.utils.ServiceVisibility;
import cloud.hytora.remote.Remote;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

public class RemoteServiceManager extends DefaultServiceManager {


    @Override
    public CloudService findFallback(CloudPlayer player) {
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
    public void handleAdd(CloudEventServiceRegistered event) {
        CloudService server = event.getCloudServer();
        this.registerService(server);
    }

    @EventListener
    public void handleRemove(CloudEventServiceUnregistered event) {
        CloudService server = this.getCachedCloudService(event.getService());
        if (server == null) {
            return;
        }
        this.unregisterService(server);
    }

    @EventListener
    public void handleUpdate(CloudEventServiceUpdate event) {
        CloudService server = event.getService();

        this.updateServerInternally(server);
    }


    @Override
    public @NotNull Task<CloudService> startService(@NotNull CloudService service) {
        Task<CloudService> task = Task.empty();

        PacketCloudEntityNode.forServerStart(service, true)
                .sendQuery()
                .setReceivers(service.getRunningNodeName())
                .execute()
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
    public CloudService thisService() throws IncompatibleDriverEnvironmentException {
        return getAllCachedServices().stream().filter(s -> s.getName().equalsIgnoreCase(Remote.getInstance().getProperty().getName())).findFirst().orElse(null);
    }

    @Override
    public void shutdownService(CloudService service) {
        CloudDriver.getInstance().getExecutor().sendPacket(PacketCloudEntityService.forRequestShutdown(service));
    }


    @Override
    public void updateService(@NotNull CloudService service, PublishingType... type) {
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
                CloudDriver.getInstance().getEventManager().callEvent(new CloudEventServiceUpdate(service), PublishingType.PROTOCOL);

                break;
        }
    }

    @Override
    public void sendPacketToService(CloudService service, IPacket packet) {
        if (service.getName().equalsIgnoreCase(Remote.getInstance().thisService().getName())) {
            CloudDriver.getInstance().getExecutor().handlePacket(null, packet);
            return;
        }
        Remote.getInstance().getClient().sendPacket(new PacketRedirecting(service.getName(), packet));
    }

}
