package cloud.hytora.driver.services.task;

import cloud.hytora.driver.DriverEnvironment;
import cloud.hytora.driver.networking.packets.group.ServerConfigurationCacheUpdatePacket;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.CloudDriver;

import cloud.hytora.driver.services.task.bundle.TaskGroup;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

@Getter
@Setter
public abstract class DefaultServiceTaskManager implements ServiceTaskManager {

    protected Collection<ServiceTask> allCachedTasks = new ArrayList<>();
    protected Collection<TaskGroup> allTaskGroups = new ArrayList<>();

    public DefaultServiceTaskManager() {
        CloudDriver.getInstance().getExecutor().registerPacketHandler((PacketHandler<ServerConfigurationCacheUpdatePacket>) (wrapper, packet) -> {
            ServiceTask packetGroup = packet.getServiceTask();
            ServiceTask preset = getTaskByNameOrNull(packetGroup.getName());

            preset.cloneInternally(packetGroup, preset);
            if (CloudDriver.getInstance().getEnvironment() == DriverEnvironment.NODE) {
                preset.update();
            }
        });
    }

    @Override
    public void setAllTaskGroups(Collection<TaskGroup> taskGroup) {
        this.allTaskGroups = taskGroup;
    }

    @Override
    public void addTask(@NotNull ServiceTask task) {
        this.allCachedTasks.add(task);
    }

    public void removeTask(@NotNull ServiceTask task) {
        this.allCachedTasks.remove(task);
    }

    @Override
    public void addTaskGroup(@NotNull TaskGroup taskGroup) {
        this.allTaskGroups.add(taskGroup);
    }

    @Override
    public void removeTaskGroup(@NotNull TaskGroup taskGroup) {
        this.allTaskGroups.remove(taskGroup);
    }
}
