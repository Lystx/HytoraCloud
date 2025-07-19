package cloud.hytora.driver.entity.services.task;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.entity.services.deployment.ServiceDeployment;
import cloud.hytora.driver.entity.services.task.bundle.TaskGroup;
import cloud.hytora.driver.entity.services.template.ServiceTemplate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The {@link ServiceTaskManager} is used to manage all {@link ServiceTask}s and {@link TaskGroup}s
 *
 * @see ServiceTask
 * @see TaskGroup
 *
 * @author Lystx
 * @since DEV-1.3
 * @version STABLE-2.0
 */
public interface ServiceTaskManager {

    /**
     * Counts the capacity for players of
     * every online proxy server to check for full slots
     */
    int countProxyPlayerCapacity();

    /**
     * Returns all internally cached {@link TaskGroup}s
     */
    @NotNull
    Collection<TaskGroup> getAllCachedTaskGroups();

    /**
     * Only use this method if you know what you are doing!!!!
     * This method overrides all the internally cached {@link TaskGroup}s
     *
     * @param taskGroups the groups to set
     */
    void setAllCachedTaskGroups(Collection<TaskGroup> taskGroups);

    /**
     * This method adds a {@link TaskGroup} to the internal cache
     * Additionally if execute on Node-Side it will automatically
     * register this group to the database if not registered yet
     * and will sync the global network cache
     *
     * @param taskGroup the group to add
     */
    void addTaskGroup(@NotNull TaskGroup taskGroup);

    /**
     * This method removes a {@link TaskGroup} from the internal cache
     * Additionally if execute on Node-Side it will automatically
     * unregister this group from the database
     * and will sync the global network cache
     *
     * @param taskGroup the group to remove
     */
    void removeTaskGroup(@NotNull TaskGroup taskGroup);

    /**
     * Retrieves a cached {@link TaskGroup} by its name
     * if it is cached internally
     * This method is not case-sensitive
     *
     * @param name the name of the group
     * @return the found group or null (if not found)
     */
    TaskGroup getCachedTaskGroup(@NotNull String name);

    /**
     * @return all internally cached {@link ServiceTask}s
     */
    @NotNull
    Collection<ServiceTask> getAllCachedTasks();

    /**
     * Only use this method if you know what you are doing!!!!
     * This method overrides all the internally cached {@link ServiceTask}s
     *
     * @param tasks the tasks to set
     */
    void setAllCachedTasks(Collection<ServiceTask> tasks);

    /**
     * This method adds a {@link ServiceTask} to the internal cache
     * Additionally if execute on Node-Side it will automatically
     * register this task to the database if not registered yet
     * and will sync the global network cache
     *
     * @param task the task to add
     */
    void addTask(@NotNull ServiceTask task);

    /**
     * This method removes a {@link ServiceTask} from the internal cache
     * Additionally if execute on Node-Side it will automatically
     * unregister this task from the database
     * and will sync the global network cache
     *
     * @param task the task to remove
     */
    void removeTask(@NotNull ServiceTask task);

    /**
     * Retrieves a cached {@link ServiceTask} by its name
     * if it is cached internally
     * This method is not case-sensitive
     *
     * @param name the name of the task
     * @return the found task or null (if not found)
     */
    ServiceTask getCachedServiceTask(@NotNull String name);

    /**
     * Retrieves a {@link Collection} of found {@link ServiceTask}s
     * that match the provided {@link cloud.hytora.driver.entity.node.INode} name
     * This method is not case-sensitive
     *
     * @param node the name of the node to match
     * @return the collection of found tasks on this node
     */
    @NotNull
    Collection<ServiceTask> getCachedServiceTasksByNode(@NotNull String node);

    /**
     * Updates a {@link ServiceTask} globally
     *
     * @param task the task to update
     */
    void update(@NotNull ServiceTask task);


    /**
     * @see CloudService#deploy(ServiceDeployment...)
     */
    void deployFile(CloudService task, File file, ServiceTemplate template, String destinationPathInTemplate);

}
