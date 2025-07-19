package cloud.hytora.driver.entity.services.task;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.common.objects.PlaceHolder;
import cloud.hytora.driver.entity.node.INode;
import cloud.hytora.driver.entity.services.ConfigurableService;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.entity.services.fallback.FallbackEntry;
import cloud.hytora.driver.entity.services.task.bundle.TaskGroup;
import cloud.hytora.driver.entity.services.template.ServiceTemplate;
import cloud.hytora.driver.entity.services.template.def.CloudTemplate;
import cloud.hytora.driver.entity.services.utils.version.ServiceVersion;
import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;
import cloud.hytora.driver.common.objects.Cloneable;
import cloud.hytora.driver.common.property.IPropertyHolder;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * The {@link ServiceTask} is the parent of a {@link CloudService}.
 * You can image the ServiceTask like a {@link Collection} of CloudServices that are all submissive to this Task.
 * The {@link ServiceTask} determines the startup-values that each {@link CloudService} gets
 * => e.g. start-args, templates, memory, maxPlayers, etc.
 * <p>
 * You can also start new {@link CloudService}s based on this configuration using {@link ServiceTask#configureFutureService()}
 * A {@link ServiceTask} is also a {@link PlaceHolder} and {@link IPropertyHolder}
 * <p>
 * This Object is updateable over the Network using {@link ServiceTask#update()}
 *
 * @see CloudService
 * @see TaskGroup
 * @author Lystx
 * @version SNAPSHOT-1.0
 */
public interface ServiceTask extends IBufferObject, PlaceHolder, IPropertyHolder, Cloneable<ServiceTask> {


    /**
     * Creates a new configuration to configure
     * a new {@link CloudService} that you want to start.
     *
     * @return the created instance
     * @see ConfigurableService
     */
    ConfigurableService configureFutureService();


    /**
     * @return the default {@link ServiceTemplate} if none is created
     */
    default ServiceTemplate getDefaultTemplate() {
        return new CloudTemplate(getName(), "default", "local", true);
    }

    /**
     * @return the name of the group
     */
    @NotNull String getName();

    /**
     * The cached {@link TaskGroup} that is the parent of this Task
     *
     * @return found instance (or null if internal errors)
     */
    TaskGroup getTaskGroup();

    /**
     * @return all {@link ServiceTemplate}s that this task has
     */
    Collection<ServiceTemplate> getTemplates();

    /**
     * The permission to access a service of this configuration
     */
    String getPermission();

    /**
     * Sets the permission of this group
     *
     * @param permission the permission to set
     */
    void setPermission(String permission);

    /**
     * @return a {@link Collection} with the provided names of
     * possible {@link INode}s that {@link CloudService}s of this task may run on
     */
    @NotNull
    Collection<String> getPossibleNodes();

    /**
     * Picks a {@link INode} out of the possible nodes
     * This method does not balance, sort or any of this stuff.
     * It simply picks the first found {@link INode} instance that is
     * available for this task
     *
     * @return found instance (or null if internal error)
     */
    INode findAnyNode();

    /**
     * Async method of {@link ServiceTask#findAnyNode()}
     *
     * @return task instance
     * @see Task
     * @see ServiceTask#findAnyNode()
     */
    @NotNull
    Task<INode> findAnyNodeAsync();

    /**
     * Unlike {@link ServiceTask#findAnyNode()} this method
     * collects every available {@link INode} instance
     * that is suitable for {@link CloudService}s of this task and
     * stores them in the given {@link Collection}
     *
     * @return collection of provided node instances
     */
    @NotNull
    Collection<INode> findPossibleNodes();

    /**
     * sets the node of the group
     *
     * @param node the node to set
     */
    void setNode(@NotNull String... node);

    /**
     * @return the max memory of a service of the group
     */
    int getMemory();

    /**
     * sets the max memory of a service of the group
     *
     * @param memory the memory to set
     */
    void setMemory(int memory);

    /**
     * @return the max players of a service of the group
     */
    int getDefaultMaxPlayers();

    /**
     * sets the max players of a service of the group
     *
     * @param defaultMaxPlayers the max players to set
     */
    void setDefaultMaxPlayers(int defaultMaxPlayers);

    /**
     * @return the percentage of players
     * on one {@link CloudService} to start a new one
     */
    int getPercentForNewServer();

    /**
     * Sets the percentage of players that may be
     * online at maximum on a {@link CloudService} of this task
     * for a new service to start
     *
     * @param percent the percentage
     */
    void setPercentForNewServer(int percent);

    /**
     * @return the minimum online services of the group
     */
    int getMinOnlineService();

    /**
     * sets the minimum online services of the group
     *
     * @param minOnlineService the amount to set
     */
    void setMinOnlineService(int minOnlineService);

    /**
     * @return the maximum online services of the group
     */
    int getMaxOnlineService();

    /**
     * sets the maximum online services of the group
     *
     * @param maxOnlineService the amount to set
     */
    void setMaxOnlineService(int maxOnlineService);

    /**
     * @return if this task is in maintenance
     */
    boolean isMaintenance();

    /**
     * Sets the maintenance value of this task
     *
     * @param maintenance if in maintenance or not
     */
    void setMaintenance(boolean maintenance);

    /**
     * @return the game server version of the group
     */
    @NotNull ServiceVersion getVersion();

    /**
     * sets the game server version of the group
     *
     * @param gameServerVersion the game server version to set
     */
    void setVersion(@NotNull ServiceVersion gameServerVersion);

    String getMotd();

    void setMotd(String motd);

    List<CloudService> getOnlineServices();

    /**
     * updates the properties of the group
     */
    void update();

    /**
     * @return the start order of this task when
     * booting up the cloud and every {@link ServiceTask} boots up
     * at the same time (the lower the startOrder, the higher the priority)
     */
    int getStartOrder();

    /**
     * The int-value of the javaVersion for this {@link ServiceTask}
     * if -1 it uses the local Java-Machine version
     *
     * @return int value of version
     */
    int getJavaVersion();

    /**
     * @return the fallback instance of this task
     */
    @NotNull
    FallbackEntry getFallback();

}
