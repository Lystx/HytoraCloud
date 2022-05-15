package cloud.hytora.driver.services.configuration;

import cloud.hytora.driver.networking.protocol.codec.buf.Bufferable;
import cloud.hytora.driver.common.SelfCloneable;
import cloud.hytora.driver.services.CloudServer;
import cloud.hytora.driver.services.fallback.FallbackEntry;
import cloud.hytora.driver.services.utils.ServiceShutdownBehaviour;
import cloud.hytora.driver.services.utils.ServiceVersion;
import org.jetbrains.annotations.NotNull;

public interface ServerConfiguration extends Bufferable, SelfCloneable<ServerConfiguration> {

    /**
     * @return the name of the group
     */
    @NotNull String getName();

    /**
     * The permission to access a service of this configuration
     */
    String getPermission();

    /**
     * Startup download entries
     */
    ConfigurationDownloadEntry[] getStartupDownloadEntries();

    /**
     * Sets the permission of this group
     *
     * @param permission the permission to set
     */
    void setPermission(@NotNull String permission);

    /**
     * @return the template of the group
     */
    @NotNull String getTemplate();

    /**
     * sets the template of the group
     *
     * @param template the template to set
     */
    void setTemplate(@NotNull String template);

    /**
     * @return the node of the group
     */
    @NotNull String getNode();

    /**
     * sets the node of the group
     *
     * @param node the node to set
     */
    void setNode(@NotNull String node);

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

    ServiceShutdownBehaviour getShutdownBehaviour();

    void setShutdownBehaviour(ServiceShutdownBehaviour behaviour);

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

    java.util.List<CloudServer> getOnlineServices();

    /**
     * updates the properties of the group
     */
    void update();

   int getStartOrder();

   int getJavaVersion();

   FallbackEntry getFallback();

}
