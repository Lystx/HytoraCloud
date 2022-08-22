package cloud.hytora.driver.services.task;

import cloud.hytora.driver.CloudDriver;

import cloud.hytora.driver.event.IEventManager;
import cloud.hytora.driver.services.task.bundle.TaskGroup;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

@Getter
@Setter
public abstract class DefaultServiceTaskManager implements ICloudServiceTaskManager {

    protected Collection<IServiceTask> allCachedTasks = new ArrayList<>();
    protected Collection<TaskGroup> allTaskGroups = new ArrayList<>();

    public DefaultServiceTaskManager() {
        CloudDriver.getInstance().getProviderRegistry().getUnchecked(IEventManager.class).registerListener(this);
    }

    @Override
    public void setAllTaskGroups(Collection<TaskGroup> taskGroup) {
        this.allTaskGroups = taskGroup;
    }

    @Override
    public void addTask(@NotNull IServiceTask task) {
        this.allCachedTasks.add(task);
    }

    public void removeTask(@NotNull IServiceTask task) {
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
