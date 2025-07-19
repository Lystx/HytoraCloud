package cloud.hytora.node.impl.handler.packet;

import cloud.hytora.document.Document;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.PublishingType;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.entity.services.ServiceManager;
import cloud.hytora.driver.networking.packets.entities.PacketCloudEntityService;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.entity.services.ConfigurableService;
import cloud.hytora.driver.entity.services.task.ServiceTask;
import cloud.hytora.driver.entity.services.template.ServiceTemplate;
import cloud.hytora.driver.entity.services.template.def.CloudTemplate;
import cloud.hytora.driver.entity.services.utils.version.ServiceVersion;

import java.util.Collection;
import java.util.UUID;

public class NodeServiceHandler implements PacketHandler<PacketCloudEntityService> {

    @Override
    public void handle(PacketChannel channel, PacketCloudEntityService packet) {

        PacketBuffer buffer = packet.buffer();

        switch (packet.getPayLoad()) {
            case CONFIG:
                UUID uniqueId = buffer.readUniqueId();
                String task = buffer.readString();
                int port = buffer.readInt();
                int memory = buffer.readInt();
                int maxPlayers = buffer.readInt();
                boolean ignoreOfLimit = buffer.readBoolean();
                String motd = buffer.readString();
                String node = buffer.readString();
                Document properties = buffer.readDocument();
                Collection<CloudTemplate> templates = buffer.readWrapperObjectCollection(CloudTemplate.class);
                ServiceVersion version = buffer.readEnum(ServiceVersion.class);
                ServiceTask serviceTask = CloudDriver.getInstance().getServiceTaskManager().getCachedServiceTask(task);

                ConfigurableService configurableService = serviceTask.configureFutureService();
                if (ignoreOfLimit) {
                    configurableService.ignoreIfLimitOfServicesReached();
                }

                configurableService
                        .uniqueId(uniqueId)
                        .node(node)
                        .motd(motd)
                        .version(version)
                        .port(port)
                        .memory(memory)
                        .maxPlayers(maxPlayers)
                        .templates(templates.toArray(new ServiceTemplate[0]))
                        .properties(properties)
                        .start();

                break;

            case UPDATE_SERVER:
                CloudService cloudService = buffer.readService();

                ServiceManager sm = CloudDriver.getInstance().getServiceManager();
                sm.updateService(cloudService, PublishingType.INTERNAL);

                break;
            case REQUEST_SHUTDOWN:

                String serverName = buffer.readString();
                ServiceManager serviceManager = CloudDriver.getInstance().getServiceManager();
                CloudService service = serviceManager.getCachedCloudService(serverName);

                if (service != null) {
                    service.shutdown();
                }
                break;
            case UPDATE_NAMETAGS:

                String server = buffer.readString();
                CloudService cachedCloudService = CloudDriver.getInstance().getServiceManager().getCachedCloudService(server);
                if (cachedCloudService != null) {
                    cachedCloudService.sendPacket(packet);
                }
                break;
        }
    }
}
