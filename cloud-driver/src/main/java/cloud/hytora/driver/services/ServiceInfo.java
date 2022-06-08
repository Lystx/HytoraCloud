package cloud.hytora.driver.services;

import cloud.hytora.document.Document;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.SelfCloneable;
import cloud.hytora.driver.exception.IncompatibleDriverEnvironment;
import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.networking.PacketSender;
import cloud.hytora.driver.networking.protocol.codec.buf.Bufferable;
import cloud.hytora.driver.services.configuration.ServerConfiguration;
import cloud.hytora.driver.services.deployment.ServiceDeployment;
import cloud.hytora.driver.services.utils.ServiceState;
import cloud.hytora.driver.services.utils.ServiceVisibility;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public interface ServiceInfo extends Bufferable, SelfCloneable<ServiceInfo>, NetworkComponent, PacketSender {

    NodeServiceInfo asCloudServer() throws IncompatibleDriverEnvironment;

    List<String> queryServiceOutput();

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
    ServerConfiguration getConfiguration();

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
    default int getOnlinePlayers() {
        return (int) CloudDriver.getInstance().getPlayerManager().getAllCachedCloudPlayers()
                .stream()
                .filter(it -> {
                    ServiceInfo service = getConfiguration().getVersion().isProxy() ? it.getProxyServer() : it.getServer();
                    return service != null && service.equals(this);
                }).count();
    }

    /**
     * @return if the service is full
     */
    default boolean isFull() {
        return this.getOnlinePlayers() >= this.getMaxPlayers();
    }


    /**
     * edits the properties of the service and update then
     *
     * @param serviceConsumer the consumer to change the properties
     */
    void edit(@NotNull Consumer<ServiceInfo> serviceConsumer);

    String getMotd();

    void setMotd(String motd);

    void executeCommand(@NotNull String commandLine);

    void update();

    long getCreationTimestamp();

    Document getProperties();

    void setProperties(Document properties);
}
