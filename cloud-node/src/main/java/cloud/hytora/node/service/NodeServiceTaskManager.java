package cloud.hytora.node.service;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.database.IDatabaseManager;
import cloud.hytora.driver.event.EventListener;
import cloud.hytora.driver.event.IEventManager;
import cloud.hytora.driver.event.defaults.task.TaskUpdateEvent;

import cloud.hytora.driver.networking.packets.DriverUpdatePacket;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.node.INodeManager;
import cloud.hytora.driver.services.task.DefaultServiceTaskManager;
import cloud.hytora.driver.services.task.packet.ServiceTaskExecutePacket;
import cloud.hytora.driver.services.task.IServiceTask;
import cloud.hytora.driver.services.task.bundle.TaskGroup;
import cloud.hytora.driver.services.template.ServiceTemplate;
import cloud.hytora.driver.services.template.TemplateStorage;
import cloud.hytora.node.NodeDriver;
import cloud.hytora.driver.database.SectionedDatabase;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;


public class NodeServiceTaskManager extends DefaultServiceTaskManager implements PacketHandler<ServiceTaskExecutePacket> {

    private final SectionedDatabase database;

    public NodeServiceTaskManager() {
        this.database = NodeDriver.getInstance().getProviderRegistry().getUnchecked(IDatabaseManager.class).getDatabase();

        // loading all database groups and configurations
        this.getAllTaskGroups().addAll(this.database.getSection(TaskGroup.class).getAll());
        this.getAllCachedTasks().addAll(this.database.getSection(IServiceTask.class).getAll());

        if (CloudDriver.getInstance().getNetworkExecutor() != null) {

            //registering packet handler
            CloudDriver.getInstance().getNetworkExecutor().registerPacketHandler(this);

            //registering events
            CloudDriver.getInstance().getProviderRegistry().getUnchecked(IEventManager.class).registerListener(this);

        }

        if (CloudDriver.getInstance().getNetworkExecutor() == null) {
            return;
        }
        if (this.getAllCachedTasks().isEmpty()) {
            CloudDriver.getInstance().getLogger().warn("There are no ServiceTasks loaded!");
            CloudDriver.getInstance().getLogger().warn("Maybe you want to create some?");
        } else {
            CloudDriver.getInstance().getLogger().info("§7Cached following TaskGroups: §b" + this.getAllTaskGroups().stream().map(TaskGroup::getName).collect(Collectors.joining("§8, §b")));
            CloudDriver.getInstance().getLogger().info("§7Cached following ServiceTasks: §b" + this.getAllCachedTasks().stream().map(IServiceTask::getName).collect(Collectors.joining("§8, §b")));
        }

    }


    @EventListener
    public void handle(TaskUpdateEvent event) {
        IServiceTask packetTask = event.getTask();
        IServiceTask task = getTaskByNameOrNull(packetTask.getName());

        if (task == null) {
            return;
        }

        CloudDriver.getInstance().getLogger().trace("Updated Task {}", task.getName());
        task.copy(packetTask);
        CloudDriver.getInstance().getProviderRegistry().getUnchecked(IEventManager.class).callEventOnlyPacketBased(new TaskUpdateEvent(task));

        NodeDriver.getInstance().getServiceQueue().dequeue();

        if (NodeDriver.getInstance().getProviderRegistry().getUnchecked(INodeManager.class) != null && NodeDriver.getInstance().getProviderRegistry().getUnchecked(INodeManager.class).isHeadNode()) {
            DriverUpdatePacket.publishUpdate(CloudDriver.getInstance().getNetworkExecutor());
        }
    }

    @Override
    public void addTask(@NotNull IServiceTask task) {
        this.database.getSection(IServiceTask.class).insert(task.getName(), task);
        if (NodeDriver.getInstance().getNetworkExecutor() != null) {
            NodeDriver.getInstance().getNetworkExecutor().sendPacketToAll(new ServiceTaskExecutePacket(task, ServiceTaskExecutePacket.ExecutionPayLoad.CREATE));
        }
        super.addTask(task);

        if (NodeDriver.getInstance().getProviderRegistry().getUnchecked(INodeManager.class) != null && NodeDriver.getInstance().getProviderRegistry().getUnchecked(INodeManager.class).isHeadNode()) {
            DriverUpdatePacket.publishUpdate(CloudDriver.getInstance().getNetworkExecutor());
        }
    }

    @Override
    public void addTaskGroup(@NotNull TaskGroup task) {
        this.database.getSection(TaskGroup.class).insert(task.getName(), task);
        super.addTaskGroup(task);

        if (NodeDriver.getInstance().getProviderRegistry().getUnchecked(INodeManager.class) != null && NodeDriver.getInstance().getProviderRegistry().getUnchecked(INodeManager.class).isHeadNode()) {
            DriverUpdatePacket.publishUpdate(CloudDriver.getInstance().getNetworkExecutor());
        }
    }

    @Override
    public void removeTaskGroup(@NotNull TaskGroup task) {
        this.database.getSection(TaskGroup.class).delete(task.getName());
        super.removeTaskGroup(task);

        if (NodeDriver.getInstance().getProviderRegistry().getUnchecked(INodeManager.class) != null && NodeDriver.getInstance().getProviderRegistry().getUnchecked(INodeManager.class).isHeadNode()) {
            DriverUpdatePacket.publishUpdate(CloudDriver.getInstance().getNetworkExecutor());
        }
    }

    @Override
    public void removeTask(@NotNull IServiceTask task) {
        this.database.getSection(IServiceTask.class).delete(task.getName());
        if (NodeDriver.getInstance().getNetworkExecutor() != null) {
            NodeDriver.getInstance().getNetworkExecutor().sendPacketToAll(new ServiceTaskExecutePacket(task, ServiceTaskExecutePacket.ExecutionPayLoad.REMOVE));
        }
        super.removeTask(task);

        if (NodeDriver.getInstance().getProviderRegistry().getUnchecked(INodeManager.class) != null && NodeDriver.getInstance().getProviderRegistry().getUnchecked(INodeManager.class).isHeadNode()) {
            DriverUpdatePacket.publishUpdate(CloudDriver.getInstance().getNetworkExecutor());
        }
    }

    @Override
    public void update(@NotNull IServiceTask task) {
        this.database.getSection(IServiceTask.class).update(task.getName(), task);
        CloudDriver.getInstance().getProviderRegistry().getUnchecked(IEventManager.class).callEventGlobally(new TaskUpdateEvent(task));
        if (NodeDriver.getInstance().getProviderRegistry().getUnchecked(INodeManager.class) != null && NodeDriver.getInstance().getProviderRegistry().getUnchecked(INodeManager.class).isHeadNode()) {
            DriverUpdatePacket.publishUpdate(CloudDriver.getInstance().getNetworkExecutor());
        }
    }

    @Override
    public void handle(PacketChannel wrapper, ServiceTaskExecutePacket packet) {

        if (packet.getPayLoad().equals(ServiceTaskExecutePacket.ExecutionPayLoad.CREATE)) {
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
    }
}
