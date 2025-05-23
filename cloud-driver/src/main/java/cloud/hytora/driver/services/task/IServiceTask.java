package cloud.hytora.driver.services.task;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.common.IPlaceHolderObject;
import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;
import cloud.hytora.driver.common.ICloneableObject;
import cloud.hytora.driver.node.INode;
import cloud.hytora.driver.property.IPropertyObject;
import cloud.hytora.driver.services.ConfigurableService;
import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.driver.services.task.bundle.TaskGroup;
import cloud.hytora.driver.services.fallback.FallbackEntry;
import cloud.hytora.driver.services.template.ServiceTemplate;
import cloud.hytora.driver.services.utils.version.ServiceVersion;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface IServiceTask extends IBufferObject, IPlaceHolderObject, IPropertyObject, ICloneableObject<IServiceTask> {

    ConfigurableService configureFutureService();


    /**
     * @return the name of the group
     */
    @NotNull String getName();

    TaskGroup getTaskGroup();

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

    @NotNull Collection<String> getPossibleNodes();

    INode findAnyNode();

    Task<INode> findAnyNodeAsync();

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

    boolean isMaintenance();

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

    java.util.List<ICloudService> getOnlineServices();

    /**
     * updates the properties of the group
     */
    void update();

   int getStartOrder();

   int getJavaVersion();

   FallbackEntry getFallback();

}
