package cloud.hytora.remote.impl.handler;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.PublishingType;
import cloud.hytora.driver.networking.packets.response.NetworkResponseState;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.entity.services.ServiceManager;
import cloud.hytora.driver.networking.packets.entities.PacketCloudEntityService;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.remote.Remote;
import cloud.hytora.remote.adapter.RemoteAdapter;

public class RemoteServiceHandler implements PacketHandler<PacketCloudEntityService> {

    @Override
    public void handle(PacketChannel channel, PacketCloudEntityService packet) {

        PacketBuffer buffer = packet.buffer();

        switch (packet.getPayLoad()) {
            case UPDATE_SERVER:
                CloudService service = buffer.readService();

                ServiceManager serviceManager = CloudDriver.getInstance().getServiceManager();
                serviceManager.updateService(service, PublishingType.INTERNAL);

                break;
            case COMMAND:
                RemoteAdapter adapter = Remote.getInstance().getAdapter();
                String command = buffer.readString();
                adapter.executeCommand(command);
                break;
            case FORCE_SHUTDOWN:
                String serviceName = buffer.readString();
                CloudDriver.getInstance().getLogger().debug("Received Shutdown-Packet");
                if (serviceName.equalsIgnoreCase(Remote.getInstance().thisService().getName())) {
                    packet.sendResponse().setState(NetworkResponseState.OK).execute();
                    CloudDriver.getInstance().getLogger().debug("  => For me");
                    Remote.getInstance().shutdown();
                }
                break;
        }

    }
}
