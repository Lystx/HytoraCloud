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

    @NotNull Collection<ServiceTask> getAllCachedTasks();

    void setAllCachedTasks(Collection<ServiceTask> tasks);

    void addTask(@NotNull ServiceTask task);

    void removeTask(@NotNull ServiceTask task);

    default @NotNull Optional<ServiceTask> getTaskByName(@NotNull String name) {
        return this.getAllCachedTasks().stream().filter(it -> it.getName().equalsIgnoreCase(name)).findAny();
    }

    default @Nullable ServiceTask getTaskByNameOrNull(@NotNull String name) {
        return this.getTaskByName(name).orElse(null);
    }

    default @NotNull List<ServiceTask> getTasksByNode(@NotNull String node) {
        return this.getAllCachedTasks().stream()
            .filter(it -> it.getPossibleNodes().contains(node))
            .collect(Collectors.toList());
    }

    void update(@NotNull ServiceTask task);

}
