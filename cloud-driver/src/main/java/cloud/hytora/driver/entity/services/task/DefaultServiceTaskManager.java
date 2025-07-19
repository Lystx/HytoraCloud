package cloud.hytora.driver.entity.services.task;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;

import cloud.hytora.driver.entity.services.task.bundle.TaskGroup;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public abstract class DefaultServiceTaskManager implements ServiceTaskManager {

    protected Collection<ServiceTask> allCachedTasks = new ArrayList<>();
    protected Collection<TaskGroup> allCachedTaskGroups = new ArrayList<>();

    public DefaultServiceTaskManager() {
        CloudDriver.getInstance().getEventManager().registerListener(this);
    }

    @Override
    public TaskGroup getCachedTaskGroup(@NotNull String name) {
        return this.getAllCachedTaskGroups().stream().filter(it -> it.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    @Override
    public int countProxyPlayerCapacity() {
        int max = 0;
        for (ServiceTask serviceTask : getAllCachedTasks().stream().filter(t -> t.getVersion().isProxy()).collect(Collectors.toList())) {
            max += serviceTask.getDefaultMaxPlayers();
        }
        return max;
    }
    public void setAllCachedTaskGroups(Collection<TaskGroup> taskGroup) {
        this.allCachedTaskGroups = taskGroup;
    }

    @Override
    public void addTask(@NotNull ServiceTask task) {
        this.allCachedTasks.add(task);
    }

    public void removeTask(@NotNull ServiceTask task) {
        this.allCachedTasks.remove(task);
    }



    public @Nullable ServiceTask getCachedServiceTask(@NotNull String name) {
        return this.getAllCachedTasks().stream().filter(it -> it.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public @NotNull Collection<ServiceTask> getCachedServiceTasksByNode(@NotNull String node) {
        return this.getAllCachedTasks().stream()
                .filter(it -> it.getPossibleNodes().contains(node))
                .collect(Collectors.toList());
    }

    @Override
    public void addTaskGroup(@NotNull TaskGroup taskGroup) {
        this.allCachedTaskGroups.add(taskGroup);
    }

    @Override
    public void removeTaskGroup(@NotNull TaskGroup taskGroup) {
        this.allCachedTaskGroups.remove(taskGroup);
    }
}
