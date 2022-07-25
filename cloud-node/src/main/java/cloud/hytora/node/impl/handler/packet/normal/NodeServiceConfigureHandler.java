package cloud.hytora.node.impl.handler.packet.normal;

import cloud.hytora.document.Document;
import cloud.hytora.driver.networking.packets.services.ServiceConfigPacket;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.services.ConfigurableService;
import cloud.hytora.driver.services.task.ServiceTask;
import cloud.hytora.driver.services.template.ServiceTemplate;
import cloud.hytora.driver.services.utils.version.ServiceVersion;

import java.util.Collection;

public class NodeServiceConfigureHandler implements PacketHandler<ServiceConfigPacket> {

    @Override
    public void handle(PacketChannel wrapper, ServiceConfigPacket packet) {

        ServiceTask serviceTask = packet.getServiceTask();
        Document properties = packet.getProperties();
        Collection<ServiceTemplate> templates = packet.getTemplates();

        String motd = packet.getMotd();
        String node = packet.getNode();
        ServiceVersion version = packet.getVersion();

        int port = packet.getPort();
        int memory = packet.getMemory();
        int maxPlayers = packet.getMaxPlayers();
        boolean ignoreOfLimit = packet.isIgnoreOfLimit();

        ConfigurableService configurableService = serviceTask.configureFutureService();
        if (ignoreOfLimit) configurableService.ignoreIfLimitOfServicesReached();

        configurableService.node(node)
                .motd(motd)
                .version(version)
                .port(port)
                .memory(memory)
                .maxPlayers(maxPlayers)
                .templates(templates.toArray(new ServiceTemplate[0]))
                .properties(properties)
                .start();
    }
}
