package cloud.hytora.node.service;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.PublishingType;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.entity.services.NodeSpecificCloudService;
import cloud.hytora.driver.entity.services.template.def.CloudTemplate;
import cloud.hytora.driver.event.listener.EventListener;
import cloud.hytora.driver.event.defaults.task.CloudEventTaskUpdate;

import cloud.hytora.driver.language.Translation;
import cloud.hytora.driver.networking.packets.cache.PacketDriverCacheUpdate;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.entity.services.task.DefaultServiceTaskManager;
import cloud.hytora.driver.networking.packets.entities.PacketServiceTask;
import cloud.hytora.driver.entity.services.task.ServiceTask;
import cloud.hytora.driver.entity.services.task.bundle.TaskGroup;
import cloud.hytora.driver.entity.services.template.ServiceTemplate;
import cloud.hytora.driver.entity.services.template.TemplateStorage;
import cloud.hytora.node.NodeDriver;
import cloud.hytora.driver.database.LocalStorage;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;


public class NodeServiceTaskManager extends DefaultServiceTaskManager implements PacketHandler<PacketServiceTask> {

    private final LocalStorage database;

    public NodeServiceTaskManager() {
        this.database = NodeDriver.getInstance().getDatabaseManager().getLocalStorage();

        // loading all database groups and configurations
        this.getAllCachedTaskGroups().addAll(this.database.getSection(TaskGroup.class).getAll());
        this.getAllCachedTasks().addAll(this.database.getSection(ServiceTask.class).getAll());

        if (CloudDriver.getInstance().getExecutor() != null) {

            //registering packet handler
            CloudDriver.getInstance().getExecutor().registerPacketHandler(this);

            //registering events
            CloudDriver.getInstance().getEventManager().registerListener(this);

        }

        if (CloudDriver.getInstance().getExecutor() == null) {
            return;
        }
        if (this.getAllCachedTasks().isEmpty()) {
            for (String line : Translation.listOf("servicetask.none.create")) {
                CloudDriver.getInstance().getLogger().warn(line);
            }
        } else {
            CloudDriver.getInstance().getLogger().info(Translation.of("servicetask.loaded.groups", this.getAllCachedTaskGroups().stream().map(TaskGroup::getName).collect(Collectors.joining("§8, %1"))));
            CloudDriver.getInstance().getLogger().info(Translation.of("servicetask.loaded.tasks", this.getAllCachedTasks().stream().map(ServiceTask::getName).collect(Collectors.joining("§8, %1"))));

        }

    }


    @EventListener
    public void handle(CloudEventTaskUpdate event) {
        ServiceTask packetTask = event.getTask();
        ServiceTask task = getCachedServiceTask(packetTask.getName());

        if (task == null) {
            return;
        }

        CloudDriver.getInstance().getLogger().trace("Updated Task {}", task.getName());
        task.clone(packetTask);
        CloudDriver.getInstance().getEventManager().callEvent(new CloudEventTaskUpdate(task), PublishingType.PROTOCOL);

        NodeDriver.getInstance().getServiceQueue().dequeue();

        if (NodeDriver.getInstance().getNodeManager() != null && NodeDriver.getInstance().getNodeManager().isHeadNode()) {
            PacketDriverCacheUpdate.publishUpdate(CloudDriver.getInstance().getExecutor());
        }
    }

    @Override
    public void addTask(@NotNull ServiceTask task) {
        this.database.getSection(ServiceTask.class).insert(task.getName(), task);
        if (NodeDriver.getInstance().getExecutor() != null) {
            NodeDriver.getInstance().getExecutor().sendPacketToAll(new PacketServiceTask(task, PacketServiceTask.ExecutionPayLoad.CREATE));
        }
        super.addTask(task);

        if (NodeDriver.getInstance().getNodeManager() != null && NodeDriver.getInstance().getNodeManager().isHeadNode()) {
            PacketDriverCacheUpdate.publishUpdate(CloudDriver.getInstance().getExecutor());
        }
    }

    @Override
    public void deployFile(CloudService task, File file, ServiceTemplate template, String destinationPathInTemplate) {
        ServiceTask serviceTask = task.getTask();
        File templateDir = new File(CloudDriver.Constants.TEMPLATES_DIR, template.buildTemplatePath());
        File destination = new File(templateDir, destinationPathInTemplate);
        try {
            FileUtils.copyFile(file, destination);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void addTaskGroup(@NotNull TaskGroup task) {
        this.database.getSection(TaskGroup.class).insert(task.getName(), task);
        super.addTaskGroup(task);

        if (NodeDriver.getInstance().getNodeManager() != null && NodeDriver.getInstance().getNodeManager().isHeadNode()) {
            PacketDriverCacheUpdate.publishUpdate(CloudDriver.getInstance().getExecutor());
        }
    }

    @Override
    public void removeTaskGroup(@NotNull TaskGroup task) {
        this.database.getSection(TaskGroup.class).delete(task.getName());
        super.removeTaskGroup(task);

        if (NodeDriver.getInstance().getNodeManager() != null && NodeDriver.getInstance().getNodeManager().isHeadNode()) {
            PacketDriverCacheUpdate.publishUpdate(CloudDriver.getInstance().getExecutor());
        }
    }

    @Override
    public void removeTask(@NotNull ServiceTask task) {
        this.database.getSection(ServiceTask.class).delete(task.getName());
        if (NodeDriver.getInstance().getExecutor() != null) {
            NodeDriver.getInstance().getExecutor().sendPacketToAll(new PacketServiceTask(task, PacketServiceTask.ExecutionPayLoad.REMOVE));
        }
        super.removeTask(task);

        if (NodeDriver.getInstance().getNodeManager() != null && NodeDriver.getInstance().getNodeManager().isHeadNode()) {
            PacketDriverCacheUpdate.publishUpdate(CloudDriver.getInstance().getExecutor());
        }
    }

    @Override
    public void update(@NotNull ServiceTask task) {
        this.database.getSection(ServiceTask.class).update(task.getName(), task);
        CloudDriver.getInstance().getEventManager().callEvent(new CloudEventTaskUpdate(task), PublishingType.GLOBAL);
        if (NodeDriver.getInstance().getNodeManager() != null && NodeDriver.getInstance().getNodeManager().isHeadNode()) {
            PacketDriverCacheUpdate.publishUpdate(CloudDriver.getInstance().getExecutor());
        }
    }

    @Override
    public void handle(PacketChannel channel, PacketServiceTask packet) {

        if (packet.getPayLoad().equals(PacketServiceTask.ExecutionPayLoad.CREATE)) {
            this.getAllCachedTasks().add(packet.getServiceTask());

            //creating templates
            for (ServiceTemplate template : packet.getServiceTask().getTaskGroup().getTemplates()) {
                TemplateStorage storage = template.getStorage();
                if (storage != null) {
                    storage.createTemplate(template);
                }
            }

            NodeDriver.getInstance().getServiceQueue().dequeue();
        } else if (packet.getPayLoad() == PacketServiceTask.ExecutionPayLoad.DEPLOY_FILE) {
            PacketBuffer buffer = packet.buffer();


            String serName = buffer.readString();
            CloudService cloudService = CloudDriver.getInstance().getServiceManager().getCachedCloudService(serName);
            NodeSpecificCloudService nodeSpecificCloudService = (NodeSpecificCloudService) cloudService;
            File workingDirectory = nodeSpecificCloudService.getWorkingDirectory();

            String destinationPathInTemplate = buffer.readString();
            ServiceTemplate template = buffer.readObject(CloudTemplate.class);
            String path = buffer.readString();


            File fileToCopy = new File(workingDirectory, path);
            File destination = new File(template.buildTemplateDirectory(), destinationPathInTemplate);

            if (fileToCopy.isDirectory()) {
                cloud.hytora.common.misc.FileUtils.copyFilesToDirectory(fileToCopy.toPath(), destination.toPath());
                return;
            }
            try {
                cloud.hytora.common.misc.FileUtils.copy(fileToCopy.toPath(), destination.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            this.getAllCachedTasks().remove(packet.getServiceTask());
        }
    }
}
