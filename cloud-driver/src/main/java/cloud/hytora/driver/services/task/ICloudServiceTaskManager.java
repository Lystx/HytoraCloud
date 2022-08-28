package cloud.hytora.driver.services.task;

import cloud.hytora.common.task.ITask;
import cloud.hytora.driver.services.task.bundle.ITaskGroup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The {@link ICloudServiceTaskManager} manages and updates all the currently existing {@link IServiceTask}s
 * From here on, you can add, remove, updateTask and filter all different {@link IServiceTask}s the way
 * you need it.
 *
 * @author Lystx
 * @since SNAPSHOT-1.1
 */
public interface ICloudServiceTaskManager {

    /**
     * All currently cached {@link ITaskGroup} instances
     */
    @NotNull
    Collection<ITaskGroup> getAllCachedTaskGroups();

    /**
     * Sets all currently cached {@link ITaskGroup} instances
     *
     * @param taskGroup the collection of groups
     */
    void setAllCachedTaskGroups(@NotNull Collection<ITaskGroup> taskGroup);

    /**
     * Registers a given {@link ITaskGroup} into the cache
     * of the current Driver Instance
     * <br>
     * If somehow this {@link ITaskGroup} is already registered on the Node-Side
     * simply nothing will happen and the group won't be registered twice.
     *
     * @param taskGroup the taskGroup to register
     */
    void registerTaskGroup(@NotNull ITaskGroup taskGroup);

    /**
     * Tries to unregister a given {@link ITaskGroup}
     * <br>
     * If somehow this {@link ITaskGroup} has not been registered on the Node-Side before
     * simply nothing will happen and the service won't be unregistered.
     *
     * @param taskGroup the taskGroup to unregister
     */
    void unregisterTaskGroup(@NotNull ITaskGroup taskGroup);

    /**
     * Retrieves an {@link ITask} that might contain an {@link ITaskGroup}
     * that matches the provided name
     *
     * @param name the name to match
     * @return task instance
     */
    @NotNull
    default ITask<ITaskGroup> getTaskGroup(@NotNull String name) {
        return ITask.newInstance(getTaskGroupOrNull(name));
    }

    /**
     * Retrieves an {@link ITaskGroup} that matches the provided name
     * Might return null, if nothing found that matches names
     *
     * @param name the name to match
     * @return task instance
     */
    default @Nullable ITaskGroup getTaskGroupOrNull(@NotNull String name) {
        return this.getAllCachedTaskGroups()
                .stream()
                .filter(it -> it.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * All currently cached {@link IServiceTask} instances
     */
    @NotNull
    Collection<IServiceTask> getAllCachedTasks();

    /**
     * Sets all currently cached {@link IServiceTask} instances
     *
     * @param tasks the collection of tasks
     */
    void setAllCachedTasks(Collection<IServiceTask> tasks);

    /**
     * Registers a given {@link IServiceTask} into the cache
     * of the current Driver Instance
     * <br>
     * If somehow this {@link IServiceTask} is already registered on the Node-Side
     * simply nothing will happen and the group won't be registered twice.
     *
     * @param task the task to register
     */
    void registerTask(@NotNull IServiceTask task);

    /**
     * Tries to unregister a given {@link IServiceTask}
     * <br>
     * If somehow this {@link IServiceTask} has not been registered on the Node-Side before
     * simply nothing will happen and the service won't be unregistered.
     *
     * @param task the task to unregister
     */
    void unregisterTask(@NotNull IServiceTask task);

    /**
     * Tries to update a given {@link IServiceTask}
     * <br>
     * If this {@link IServiceTask} is not registered for any reason
     * simply nothing will happen and the method just returns
     *
     * @param task the task to update
     */
    void updateTask(@NotNull IServiceTask task);

    /**
     * Retrieves an {@link ITask} that might contain an {@link IServiceTask}
     * that matches the provided name
     *
     * @param name the name to match
     * @return task instance
     */
    @NotNull
    default ITask<IServiceTask> getTask(@NotNull String name) {
        return ITask.callAsync(() -> this.getAllCachedTasks().stream().filter(it -> it.getName().equalsIgnoreCase(name)).findFirst().orElse(null));
    }

    /**
     * Retrieves an {@link IServiceTask} that matches the provided name
     * Might return null, if nothing found that matches names
     *
     * @param name the name to match
     * @return task instance
     */
    @Nullable
    default IServiceTask getTaskOrNull(@NotNull String name) {
        return this.getAllCachedTasks().stream().filter(it -> it.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    /**
     * Retrieves a {@link Collection} of all {@link IServiceTask}s that may
     * run on a node that matches the provided node name
     *
     * @param node the name of the node
     * @return collection of tasks
     */
    @NotNull
    default Collection<IServiceTask> getTasksByNode(@NotNull String node) {
        return this.getAllCachedTasks()
                .stream()
                .filter(it -> it.getPossibleNodes().contains(node))
                .collect(Collectors.toList());
    }


}
