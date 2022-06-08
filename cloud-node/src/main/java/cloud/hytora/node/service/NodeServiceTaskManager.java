package cloud.hytora.node.service;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.event.EventListener;
import cloud.hytora.driver.event.defaults.task.TaskUpdateEvent;

import cloud.hytora.driver.services.task.DefaultServiceTaskManager;
import cloud.hytora.driver.networking.packets.group.ServiceConfigurationExecutePacket;
import cloud.hytora.driver.networking.packets.group.ServerConfigurationCacheUpdatePacket;
import cloud.hytora.driver.services.task.ServiceTask;
import cloud.hytora.driver.services.task.bundle.TaskGroup;
import cloud.hytora.driver.services.template.ServiceTemplate;
import cloud.hytora.driver.services.template.TemplateStorage;
import cloud.hytora.node.NodeDriver;
import cloud.hytora.node.impl.database.impl.SectionedDatabase;
import cloud.hytora.driver.networking.protocol.packets.ConnectionType;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;


public class NodeServiceTaskManager extends DefaultServiceTaskManager {

    private final SectionedDatabase database;

    public NodeServiceTaskManager() {
        this.database = NodeDriver.getInstance().getDatabaseManager().getDatabase();

        // loading all database groups and configurations
        this.getAllTaskGroups().addAll(this.database.getSection(TaskGroup.class).getAll());
        this.getAllCachedTasks().addAll(this.database.getSection(ServiceTask.class).getAll());

        CloudDriver.getInstance().getExecutor().registerPacketHandler((PacketHandler<ServiceConfigurationExecutePacket>) (ctx, packet) -> {
            if (packet.getPayLoad().equals(ServiceConfigurationExecutePacket.ExecutionPayLoad.CREATE)) {
                this.getAllCachedTasks().add(packet.getServiceTask());

                //creating templates
                for (ServiceTemplate template : packet.getServiceTask().getTaskGroup().getTemplates()) {
                    TemplateStorage storage = template.getStorage();
                    if (storage != null) {
                        storage.createTemplate(template);
                    }
                }

                NodeDriver.getInstance().getServiceQueue().dequeue();
            } else {
                this.getAllCachedTasks().remove(packet.getServiceTask());
            }
        });

        //registering events
        CloudDriver.getInstance().getEventManager().registerListener(this);

        if (this.getAllCachedTasks().isEmpty()) {
            CloudDriver.getInstance().getLogger().warn("There are no ServiceConfigurations loaded!");
            CloudDriver.getInstance().getLogger().warn("Maybe you want to create some?");
        } else {
            CloudDriver.getInstance().getLogger().info("§7Cached following groups: §b" + this.getAllTaskGroups().stream().map(TaskGroup::getName).collect(Collectors.joining("§8, §b")));
            CloudDriver.getInstance().getLogger().info("§7Cached following configurations: §b" + this.getAllCachedTasks().stream().map(ServiceTask::getName).collect(Collectors.joining("§8, §b")));
        }

    }

    @EventListener
    public void handle(TaskUpdateEvent event) {
        NodeDriver.getInstance().getServiceQueue().dequeue();
    }

    @Override
    public void addTask(@NotNull ServiceTask task) {
        this.database.getSection(ServiceTask.class).insert(task.getName(), task);
        NodeDriver.getInstance().getExecutor().sendPacketToAll(new ServiceConfigurationExecutePacket(task, ServiceConfigurationExecutePacket.ExecutionPayLoad.CREATE));
        super.addTask(task);
    }

    @Override
    public void addTaskGroup(@NotNull TaskGroup taskGroup) {
        this.database.getSection(TaskGroup.class).insert(taskGroup.getName(), taskGroup);
        super.addTaskGroup(taskGroup);
    }

    @Override
    public void removeTaskGroup(@NotNull TaskGroup taskGroup) {
        this.database.getSection(TaskGroup.class).delete(taskGroup.getName());
        super.removeTaskGroup(taskGroup);
    }

    @Override
    public void removeTask(@NotNull ServiceTask task) {
        this.database.getSection(ServiceTask.class).delete(task.getName());
        NodeDriver.getInstance().getExecutor().sendPacketToAll(new ServiceConfigurationExecutePacket(task, ServiceConfigurationExecutePacket.ExecutionPayLoad.REMOVE));
        super.removeTask(task);
    }

    @Override
    public void update(@NotNull ServiceTask task) {
        ServerConfigurationCacheUpdatePacket packet = new ServerConfigurationCacheUpdatePacket(task);
        // update all other nodes and this service groups
        NodeDriver.getInstance().getExecutor().sendPacketToType(packet, ConnectionType.NODE);
        // update own service group caches
        NodeDriver.getInstance().getExecutor().sendPacketToType(packet, ConnectionType.SERVICE);

        NodeDriver.getInstance().getServiceQueue().dequeue();
    }

}
