package cloud.hytora.driver.services.task;

import cloud.hytora.driver.services.task.bundle.TaskGroup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public interface ServiceTaskManager {

    Collection<TaskGroup> getAllTaskGroups();

    void setAllTaskGroups(Collection<TaskGroup> taskGroup);

    void addTaskGroup(@NotNull TaskGroup taskGroup);

    void removeTaskGroup(@NotNull TaskGroup taskGroup);

    default @NotNull Optional<TaskGroup> getTaskGroupByName(@NotNull String name) {
        return this.getAllTaskGroups().stream().filter(it -> it.getName().equalsIgnoreCase(name)).findAny();
    }

    default @Nullable TaskGroup getTaskGroupByNameOrNull(@NotNull String name) {
        return this.getTaskGroupByName(name).orElse(null);
    }

    @NotNull Collection<IServiceTask> getAllCachedTasks();

    void setAllCachedTasks(Collection<IServiceTask> tasks);

    void addTask(@NotNull IServiceTask task);

    void removeTask(@NotNull IServiceTask task);

    default @NotNull Optional<IServiceTask> getTaskByName(@NotNull String name) {
        return this.getAllCachedTasks().stream().filter(it -> it.getName().equalsIgnoreCase(name)).findAny();
    }

    default @Nullable IServiceTask getTaskByNameOrNull(@NotNull String name) {
        return this.getTaskByName(name).orElse(null);
    }

    default @NotNull List<IServiceTask> getTasksByNode(@NotNull String node) {
        return this.getAllCachedTasks().stream()
            .filter(it -> it.getPossibleNodes().contains(node))
            .collect(Collectors.toList());
    }

    void update(@NotNull IServiceTask task);

}
