package cloud.hytora.driver.services.task;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.services.task.bundle.TaskGroup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

// TODO: 04.08.2022 documentation and remove Optionals (Tasks)
public interface ServiceTaskManager {


    default int countPlayerCapacity() {
        int max = 0;
        for (IServiceTask serviceTask : getAllCachedTasks().stream().filter(t -> t.getVersion().isProxy()).collect(Collectors.toList())) {
            max += serviceTask.getDefaultMaxPlayers();
        }
        return max;
    }

    Collection<TaskGroup> getAllTaskGroups();

    void setAllTaskGroups(Collection<TaskGroup> taskGroup);

    void addTaskGroup(@NotNull TaskGroup taskGroup);

    void removeTaskGroup(@NotNull TaskGroup taskGroup);

    default @Nullable TaskGroup getCachedTaskGroup(@NotNull String name) {
        return this.getAllTaskGroups().stream().filter(it -> it.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    @NotNull Collection<IServiceTask> getAllCachedTasks();

    void setAllCachedTasks(Collection<IServiceTask> tasks);

    void addTask(@NotNull IServiceTask task);

    void removeTask(@NotNull IServiceTask task);

    default @NotNull Task<IServiceTask> getServiceTask(@NotNull String name) {
        return Task.callAsync(() -> {
            return this.getAllCachedTasks().stream().filter(it -> it.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
        });
    }

    default @Nullable IServiceTask getCachedServiceTask(@NotNull String name) {
        return this.getAllCachedTasks().stream().filter(it -> it.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    default @NotNull List<IServiceTask> getTasksByNode(@NotNull String node) {
        return this.getAllCachedTasks().stream()
            .filter(it -> it.getPossibleNodes().contains(node))
            .collect(Collectors.toList());
    }

    void update(@NotNull IServiceTask task);

}
