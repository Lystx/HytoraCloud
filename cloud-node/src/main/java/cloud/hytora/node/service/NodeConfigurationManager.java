package cloud.hytora.node.service;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.event.EventListener;
import cloud.hytora.driver.event.defaults.server.CloudServiceGroupUpdateEvent;

import cloud.hytora.driver.services.configuration.DefaultConfigurationManager;
import cloud.hytora.driver.networking.packets.group.ServiceConfigurationExecutePacket;
import cloud.hytora.driver.networking.packets.group.ServerConfigurationCacheUpdatePacket;
import cloud.hytora.driver.services.configuration.ServerConfiguration;
import cloud.hytora.driver.services.configuration.bundle.ConfigurationParent;
import cloud.hytora.driver.services.template.ServiceTemplate;
import cloud.hytora.driver.services.template.TemplateStorage;
import cloud.hytora.node.NodeDriver;
import cloud.hytora.node.impl.database.impl.SectionedDatabase;
import cloud.hytora.driver.networking.protocol.packets.ConnectionType;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;


public class NodeConfigurationManager extends DefaultConfigurationManager {

    private final SectionedDatabase database;

    public NodeConfigurationManager() {
        this.database = NodeDriver.getInstance().getDatabaseManager().getDatabase();

        // loading all database groups and configurations
        this.getAllParentConfigurations().addAll(this.database.getSection(ConfigurationParent.class).getAll());
        this.getAllCachedConfigurations().addAll(this.database.getSection(ServerConfiguration.class).getAll());

        CloudDriver.getInstance().getExecutor().registerPacketHandler((PacketHandler<ServiceConfigurationExecutePacket>) (ctx, packet) -> {
            if (packet.getPayLoad().equals(ServiceConfigurationExecutePacket.ExecutionPayLoad.CREATE)) {
                getAllCachedConfigurations().add(packet.getConfiguration());

                //creating templates
                for (ServiceTemplate template : packet.getConfiguration().getParent().getTemplates()) {
                    TemplateStorage storage = template.getStorage();
                    if (storage != null) {
                        storage.createTemplate(template);
                    }
                }

                NodeDriver.getInstance().getServiceQueue().dequeue();
            } else {
                this.getAllCachedConfigurations().remove(packet.getConfiguration());
            }
        });

        //registering events
        CloudDriver.getInstance().getEventManager().registerListener(this);

        if (this.getAllCachedConfigurations().isEmpty()) {
            CloudDriver.getInstance().getLogger().warn("There are no ServiceConfigurations loaded!");
            CloudDriver.getInstance().getLogger().warn("Maybe you want to create some?");
        } else {
            CloudDriver.getInstance().getLogger().info("§7Cached following groups: §b" + this.getAllParentConfigurations().stream().map(ConfigurationParent::getName).collect(Collectors.joining("§8, §b")));
            CloudDriver.getInstance().getLogger().info("§7Cached following configurations: §b" + this.getAllCachedConfigurations().stream().map(ServerConfiguration::getName).collect(Collectors.joining("§8, §b")));
        }

    }

    @EventListener
    public void handle(CloudServiceGroupUpdateEvent event) {
        NodeDriver.getInstance().getServiceQueue().dequeue();
    }

    @Override
    public void addConfiguration(@NotNull ServerConfiguration serviceGroup) {
        this.database.getSection(ServerConfiguration.class).insert(serviceGroup.getName(), serviceGroup);
        NodeDriver.getInstance().getExecutor().sendPacketToAll(new ServiceConfigurationExecutePacket(serviceGroup, ServiceConfigurationExecutePacket.ExecutionPayLoad.CREATE));
        super.addConfiguration(serviceGroup);
    }

    @Override
    public void addParentConfiguration(@NotNull ConfigurationParent serviceGroup) {
        this.database.getSection(ConfigurationParent.class).insert(serviceGroup.getName(), serviceGroup);
        super.addParentConfiguration(serviceGroup);
    }

    @Override
    public void removeParentConfiguration(@NotNull ConfigurationParent serviceGroup) {
        this.database.getSection(ConfigurationParent.class).delete(serviceGroup.getName());
        super.removeParentConfiguration(serviceGroup);
    }

    @Override
    public void removeConfiguration(@NotNull ServerConfiguration serviceGroup) {
        this.database.getSection(ServerConfiguration.class).delete(serviceGroup.getName());
        NodeDriver.getInstance().getExecutor().sendPacketToAll(new ServiceConfigurationExecutePacket(serviceGroup, ServiceConfigurationExecutePacket.ExecutionPayLoad.REMOVE));
        super.removeConfiguration(serviceGroup);
    }

    @Override
    public void update(@NotNull ServerConfiguration serviceGroup) {
        ServerConfigurationCacheUpdatePacket packet = new ServerConfigurationCacheUpdatePacket(serviceGroup);
        // update all other nodes and this service groups
        NodeDriver.getInstance().getExecutor().sendPacketToType(packet, ConnectionType.NODE);
        // update own service group caches
        NodeDriver.getInstance().getExecutor().sendPacketToType(packet, ConnectionType.SERVICE);

        NodeDriver.getInstance().getServiceQueue().dequeue();
    }

}
