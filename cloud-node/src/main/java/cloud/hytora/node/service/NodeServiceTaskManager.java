package cloud.hytora.node.service;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.DriverEnvironment;
import cloud.hytora.driver.event.EventListener;
import cloud.hytora.driver.event.defaults.task.TaskUpdateEvent;

import cloud.hytora.driver.services.task.DefaultServiceTaskManager;
import cloud.hytora.driver.networking.packets.group.ServiceTaskExecutePacket;
import cloud.hytora.driver.services.task.ServiceTask;
import cloud.hytora.driver.services.task.bundle.TaskGroup;
import cloud.hytora.driver.services.template.ServiceTemplate;
import cloud.hytora.driver.services.template.TemplateStorage;
import cloud.hytora.node.NodeDriver;
import cloud.hytora.node.impl.database.impl.SectionedDatabase;
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

        CloudDriver.getInstance().getExecutor().registerPacketHandler((PacketHandler<ServiceTaskExecutePacket>) (ctx, packet) -> {
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
        });

        //registering events
        CloudDriver.getInstance().getEventManager().registerListener(this);

        if (this.getAllCachedTasks().isEmpty()) {
            CloudDriver.getInstance().getLogger().warn("There are no ServiceTasks loaded!");
            CloudDriver.getInstance().getLogger().warn("Maybe you want to create some?");
        } else {
            CloudDriver.getInstance().getLogger().info("§7Cached following TaskGroups: §b" + this.getAllTaskGroups().stream().map(TaskGroup::getName).collect(Collectors.joining("§8, §b")));
            CloudDriver.getInstance().getLogger().info("§7Cached following ServiceTasks: §b" + this.getAllCachedTasks().stream().map(ServiceTask::getName).collect(Collectors.joining("§8, §b")));
        }

    }


    @EventListener
    public void handle(TaskUpdateEvent event) {
        NodeDriver.getInstance().getServiceQueue().dequeue();
        ServiceTask packetTask = event.getTask();
        ServiceTask task = getTaskByNameOrNull(packetTask.getName());

        if (task == null) {
            return;
        }

        CloudDriver.getInstance().getLogger().trace("Updated Task {}", task.getName());
        task.cloneInternally(packetTask, task);
        CloudDriver.getInstance().getEventManager().callEventOnlyPacketBased(new TaskUpdateEvent(task));
    }

    @Override
    public void addTask(@NotNull ServiceTask task) {
        this.database.getSection(ServiceTask.class).insert(task.getName(), task);
        NodeDriver.getInstance().getExecutor().sendPacketToAll(new ServiceTaskExecutePacket(task, ServiceTaskExecutePacket.ExecutionPayLoad.CREATE));
        super.addTask(task);
    }

    @Override
    public void addTaskGroup(@NotNull TaskGroup task) {
        this.database.getSection(TaskGroup.class).insert(task.getName(), task);
        super.addTaskGroup(task);
    }

    @Override
    public void removeTaskGroup(@NotNull TaskGroup task) {
        this.database.getSection(TaskGroup.class).delete(task.getName());
        super.removeTaskGroup(task);
    }

    @Override
    public void removeTask(@NotNull ServiceTask task) {
        this.database.getSection(ServiceTask.class).delete(task.getName());
        NodeDriver.getInstance().getExecutor().sendPacketToAll(new ServiceTaskExecutePacket(task, ServiceTaskExecutePacket.ExecutionPayLoad.REMOVE));
        super.removeTask(task);
    }

    @Override
    public void update(@NotNull ServiceTask task) {
        this.database.getSection(ServiceTask.class).update(task.getName(), task);
        CloudDriver.getInstance().getEventManager().callEventGlobally(new TaskUpdateEvent(task));
    }

}
