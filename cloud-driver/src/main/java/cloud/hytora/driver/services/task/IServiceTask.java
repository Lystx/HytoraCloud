package cloud.hytora.driver.services.task;

import cloud.hytora.common.task.IPromise;
import cloud.hytora.driver.common.IPlaceHolderObject;
import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;
import cloud.hytora.driver.common.ICopyableObject;
import cloud.hytora.driver.node.INode;
import cloud.hytora.driver.node.config.JavaVersion;
import cloud.hytora.driver.property.IPropertyObject;
import cloud.hytora.driver.services.IFutureCloudServer;
import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.driver.services.task.bundle.ITaskGroup;
import cloud.hytora.driver.services.fallback.ICloudFallback;
import cloud.hytora.driver.services.template.ITemplate;
import cloud.hytora.driver.services.utils.version.ServiceVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * The {@link IServiceTask} is practically a configuration for certain {@link ICloudServer}
 * that contains default values for every new {@link ICloudServer} that is being started with this "group"
 * for the server itself
 *
 * @author Lystx
 * @since SNAPSHOT-1.0
 */
public interface IServiceTask extends IBufferObject, IPlaceHolderObject, IPropertyObject, ICopyableObject<IServiceTask> {

    /**
     * Returns a new {@link IFutureCloudServer} that you can configure to start
     *
     * @return instance
     * @see IFutureCloudServer
     */
    @NotNull
    IFutureCloudServer configureFutureService();

    /**
     * The name of this task
     */
    @NotNull
    String getName();

    /**
     * The {@link ITaskGroup} of this task
     *
     * @see ITaskGroup
     */
    @NotNull
    ITaskGroup getTaskGroup();

    /**
     * Returns all {@link ITemplate}s that are being used for this task
     */
    @NotNull
    Collection<ITemplate> getTemplates();

    /**
     * The permission to access a service of this task
     */
    @Nullable
    String getPermission();

    /**
     * Sets the permission of this task
     *
     * @param permission the permission
     * @see #getPermission()
     */
    void setPermission(@Nullable String permission);

    /**
     * Returns a {@link Collection} of node-names that services
     * of this task can possibly run on
     */
    @NotNull
    Collection<String> getPossibleNodes();

    /**
     * Returns any {@link INode} that services of this task can run on
     * <b>ATTENTION:</b> Might be null if {@link #getPossibleNodes()} is empty
     */
    @Nullable
    INode findAnyNode();

    /**
     * {@link #findAnyNode()} but async and returns an {@link IPromise}
     *
     * @return task instance
     * @see IPromise
     * @see #getPossibleNodes()
     */
    @NotNull
    IPromise<INode> findAnyNodeAsync();

    /**
     * Returns a {@link Collection} of all {@link INode} instances
     * that services of this task can run on
     *
     * @see #getPossibleNodes()
     */
    @NotNull
    Collection<INode> findPossibleNodes();

    /**
     * Sets the possible nodes of the task
     *
     * @param node the node to set
     */
    void setNode(@NotNull String... node);

    /**
     * Returns the default maximum amount of memory in MB for an {@link ICloudServer}
     * of this task.<br>
     * <b>NOTE:</b> you can still modify the memory of a single {@link ICloudServer}
     */
    int getMemory();

    /**
     * Sets the max memory of a service of this task
     *
     * @param memory the memory to set
     * @see #getMemory()
     */
    void setMemory(int memory);

    /**
     * Returns the default-max players on a service of this task
     */
    int getDefaultMaxPlayers();

    /**
     * Sets the default-max players of a service of this task
     *
     * @param defaultMaxPlayers the max players to set
     */
    void setDefaultMaxPlayers(int defaultMaxPlayers);

    /**
     * Returns the minimum online services of this task
     */
    int getMinOnlineService();

    /**
     * Sets the minimum online services of this task
     *
     * @param minOnlineService the amount to set
     */
    void setMinOnlineService(int minOnlineService);

    /**
     * Returns the maximum online services of this task
     */
    int getMaxOnlineService();

    /**
     * Sets the maximum online services of this task
     *
     * @param maxOnlineService the amount to set
     */
    void setMaxOnlineService(int maxOnlineService);

    /**
     * If this task is in maintenance
     */
    boolean isMaintenance();

    /**
     * Sets the maintenance state of this task
     *
     * @param maintenance the state
     */
    void setMaintenance(boolean maintenance);

    /**
     * Returns the {@link ServiceVersion} that {@link ICloudServer}s
     * of this task will use
     */
    @NotNull
    ServiceVersion getVersion();

    /**
     * Sets the {@link ServiceVersion} of this task
     *
     * @param version the version to set
     * @see ServiceVersion
     * @see #getVersion()
     */
    void setVersion(@NotNull ServiceVersion version);

    /**
     * The default motd for services of this task
     */
    @NotNull
    String getMotd();

    /**
     * Sets the default motd for services of this task
     *
     * @param motd the motd to set
     */
    void setMotd(@NotNull String motd);

    /**
     * Returns a {@link Collection} of all {@link ICloudServer}s
     * that are currently online and that have this {@link IServiceTask}
     */
    @NotNull
    Collection<ICloudServer> getOnlineServices();

    /**
     * Updates this {@link IServiceTask} within the whole cluster
     * and sends every participant the update of this task instance
     */
    void update();

    /**
     * Returns the start-order to filter on cloud-bootup which task has higher priority
     * <b>NOTE:</b> The lower => the higher the priority
     */
    int getStartOrder();

    /**
     * The id of the java-version that has to be registered in the main-config of the node
     *
     * @see JavaVersion
     */
    int getJavaVersion();

    /**
     * Returns the default {@link ICloudFallback} for this task
     *
     * @see ICloudFallback
     */
    ICloudFallback getFallback();

}
