package cloud.hytora.driver.services;

import cloud.hytora.common.identification.ModifiableUUIDHolder;
import cloud.hytora.common.task.Task;
import cloud.hytora.document.Document;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.IClusterObject;
import cloud.hytora.driver.exception.IncompatibleDriverEnvironment;
import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.networking.IPacketExecutor;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.player.ICloudPlayerManager;
import cloud.hytora.driver.services.task.IServiceTask;
import cloud.hytora.driver.services.deployment.ServiceDeployment;
import cloud.hytora.driver.services.utils.ServiceState;
import cloud.hytora.driver.services.utils.ServiceVisibility;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

// TODO: 04.08.2022 documentation
public interface ICloudServer extends IClusterObject<ICloudServer>, NetworkComponent, IPacketExecutor, ModifiableUUIDHolder {

    IServiceCycleData getLastCycleData();

    boolean isRegisteredAsFallback();

    ServicePingProperties getPingProperties();

    Document getProperties();

    void setProperties(Document properties);

    void editPingProperties(Consumer<ServicePingProperties> ping);

    void setLastCycleData(IServiceCycleData data);

    String getRunningNodeName();

    void setRunningNodeName(String name);

    void deploy(ServiceDeployment... deployments);

    boolean isTimedOut();

    boolean isReady();

    void shutdown();

    void setReady(boolean ready);

    /**
     * @return the service id
     */
    int getServiceID();

    /**
     * @return the port of the service
     */
    int getPort();

    /**
     * @return the host name of the service
     */
    @NotNull String getHostName();

    String getReadableUptime();

    /**
     * @return the group of the service
     */
    IServiceTask getTask();

    Task<IServiceTask> getTaskAsync();

    /**
     * @return the state of the service
     */
    @NotNull ServiceState getServiceState();

    /**
     * sets the service state
     *
     * @param serviceState the state to set
     */
    void setServiceState(@NotNull ServiceState serviceState);

    /**
     * @return the max players of the service
     */
    int getMaxPlayers();

    /**
     * sets the max players of the service
     *
     * @param slots the amount to set
     */
    void setMaxPlayers(int slots);

    /**
     * @return the service visibility of the service
     */
    @NotNull ServiceVisibility getServiceVisibility();

    /**
     * sets the service visibility
     *
     * @param serviceVisibility the service visibility to set
     */
    void setServiceVisibility(@NotNull ServiceVisibility serviceVisibility);

    /**
     * @return the online amount of the service
     */
    default int getOnlinePlayerCount() {
        return (int) CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudPlayerManager.class).getAllCachedCloudPlayers()
                .stream()
                .filter(it -> {
                    ICloudServer service = getTask().getVersion().isProxy() ? it.getProxyServer() : it.getServer();
                    return service != null && service.equals(this);
                }).count();
    }

    /**
     * @return the online amount of the service
     */
    default Collection<ICloudPlayer> getOnlinePlayers() {
        return CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudPlayerManager.class).getAllCachedCloudPlayers()
                .stream()
                .filter(it -> {
                    ICloudServer service = getTask().getVersion().isProxy() ? it.getProxyServer() : it.getServer();
                    return service != null && service.equals(this);
                }).collect(Collectors.toList());
    }

    /**
     * @return if the service is full
     */
    default boolean isFull() {
        return this.getOnlinePlayerCount() >= this.getMaxPlayers();
    }

    String getMotd();

    void setMotd(String motd);

    void sendCommand(@NotNull String commandLine);

    void update();

    long getCreationTimestamp();

}
