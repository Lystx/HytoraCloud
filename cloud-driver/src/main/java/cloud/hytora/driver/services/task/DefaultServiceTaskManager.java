package cloud.hytora.driver.services.task;

import cloud.hytora.driver.CloudDriver;

import cloud.hytora.driver.event.IEventManager;
import cloud.hytora.driver.services.task.bundle.ITaskGroup;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

@Getter
@Setter
public abstract class DefaultServiceTaskManager implements ICloudServiceTaskManager {

    protected Collection<IServiceTask> allCachedTasks = new ArrayList<>();
    protected Collection<ITaskGroup> allCachedTaskGroups = new ArrayList<>();

    public DefaultServiceTaskManager() {
        CloudDriver.getInstance().getProviderRegistry().getUnchecked(IEventManager.class).registerListener(this);
    }

    public void setAllCachedTaskGroups(@NotNull Collection<ITaskGroup> taskGroup) {
        this.allCachedTaskGroups = taskGroup;
    }

    @Override
    public void registerTask(@NotNull IServiceTask task) {
        this.allCachedTasks.add(task);
    }

    public void unregisterTask(@NotNull IServiceTask task) {
        this.allCachedTasks.remove(task);
    }

    @Override
    public void registerTaskGroup(@NotNull ITaskGroup taskGroup) {
        this.allCachedTaskGroups.add(taskGroup);
    }

    @Override
    public void unregisterTaskGroup(@NotNull ITaskGroup taskGroup) {
        this.allCachedTaskGroups.remove(taskGroup);
    }
}
