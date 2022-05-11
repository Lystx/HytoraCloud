package cloud.hytora.node.service;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.event.CloudEventHandler;
import cloud.hytora.driver.event.defaults.server.CloudServiceGroupUpdateEvent;

import cloud.hytora.driver.services.configuration.DefaultConfigurationManager;
import cloud.hytora.driver.networking.packets.group.ServiceGroupExecutePacket;
import cloud.hytora.driver.networking.packets.group.ServerConfigurationCacheUpdatePacket;
import cloud.hytora.driver.services.configuration.ServerConfiguration;
import cloud.hytora.node.NodeDriver;
import cloud.hytora.node.impl.database.IDatabase;
import cloud.hytora.driver.networking.protocol.packets.ConnectionType;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;


public class NodeConfigurationManager extends DefaultConfigurationManager {

    private final IDatabase database;

    public NodeConfigurationManager() {
        this.database = NodeDriver.getInstance().getDatabaseManager().getDatabase();

        // loading all database groups
        this.getAllCachedConfigurations().addAll(this.database.getAllServiceGroups());

        CloudDriver.getInstance().getExecutor().registerPacketHandler((PacketHandler<ServiceGroupExecutePacket>) (ctx, packet) -> {
            if (packet.getPayLoad().equals(ServiceGroupExecutePacket.ExecutionPayLoad.CREATE)) {
                getAllCachedConfigurations().add(packet.getGroup());
                NodeDriver.getInstance().getNodeTemplateService().createTemplateFolder(packet.getGroup());
                NodeDriver.getInstance().getServiceQueue().dequeue();
            } else {
                this.getAllCachedConfigurations().remove(packet.getGroup());
            }
        });

        //registering events
        CloudDriver.getInstance().getEventManager().registerListener(this);

        if (this.getAllCachedConfigurations().isEmpty()) {
            CloudDriver.getInstance().getLogger().warn("There are no ServiceConfigurations loaded!");
            CloudDriver.getInstance().getLogger().warn("Maybe you want to create some?");
        } else {
            CloudDriver.getInstance().getLogger().info("§7Cached following groups: §b" + this.getAllCachedConfigurations().stream().map(ServerConfiguration::getName).collect(Collectors.joining("§8, §b")));
        }
    }

    @CloudEventHandler
    public void handle(CloudServiceGroupUpdateEvent event) {
        NodeDriver.getInstance().getServiceQueue().dequeue();
    }

    @Override
    public void addConfiguration(@NotNull ServerConfiguration serviceGroup) {
        this.database.addGroup(serviceGroup);
        NodeDriver.getInstance().getExecutor().sendPacketToAll(new ServiceGroupExecutePacket(serviceGroup, ServiceGroupExecutePacket.ExecutionPayLoad.CREATE));
        super.addConfiguration(serviceGroup);
    }


    @Override
    public void removeConfiguration(@NotNull ServerConfiguration serviceGroup) {
        this.database.removeGroup(serviceGroup);
        NodeDriver.getInstance().getExecutor().sendPacketToAll(new ServiceGroupExecutePacket(serviceGroup, ServiceGroupExecutePacket.ExecutionPayLoad.REMOVE));
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
