package cloud.hytora.driver.entity.services;

import cloud.hytora.common.identification.ModifiableUUIDHolder;
import cloud.hytora.driver.common.PublishingType;
import cloud.hytora.driver.common.objects.NetworkEntity;
import cloud.hytora.driver.common.message.base.ChannelMessage;
import cloud.hytora.driver.entity.CloudEntity;
import cloud.hytora.driver.entity.player.connection.ProtocolVersion;
import cloud.hytora.driver.entity.services.task.bundle.TaskGroup;
import cloud.hytora.driver.entity.services.template.ServiceTemplate;
import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.networking.PacketSender;
import cloud.hytora.driver.entity.node.INode;
import cloud.hytora.driver.entity.player.CloudPlayer;
import cloud.hytora.driver.common.property.IPropertyHolder;
import cloud.hytora.driver.entity.services.task.ServiceTask;
import cloud.hytora.driver.entity.services.deployment.ServiceDeployment;
import cloud.hytora.driver.entity.services.utils.ServiceState;
import cloud.hytora.driver.entity.services.utils.ServiceVisibility;

import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * The {@link CloudService} are the children of {@link ServiceTask}
 * Here the real magic happens on the network. Every {@link CloudPlayer} is on at least one {@link CloudService} instance.<br>
 * This object can function as Proxy and as Minecraft-Server. There is no differentiation between object instances.
 * U can check for the type using {@link TaskGroup#getEnvironment()}
 * <br>
 * <p>This Object is updateable over the Network using {@link CloudService#update(PublishingType...)}</p>
 *
 * @author Lystx
 * @version SNAPSHOT-1.0
 * @see ServiceTask
 * @see TaskGroup
 */
public interface CloudService extends NetworkEntity<CloudService>, CloudEntity, NetworkComponent, PacketSender, ModifiableUUIDHolder, IPropertyHolder {


    /**
     * Checks the {@link ProtocolVersion} of the player
     * and of the {@link CloudService} to check if the player is
     * able to even connect to this service
     * otherwise it will be denied
     *
     * @param player the player to check
     * @return true if can join
     */
    ProtocolVersion.SwitchResult canJoin(CloudPlayer player);

    /**
     * The last data that was cached for this {@link CloudService}
     *
     * @return instance
     * @see ServiceCycleData
     */
    @NotNull
    ServiceCycleData getLastCycleData();

    /**
     * Creates a simulated {@link PacketChannel} that is not based
     * on the real internal netty-channel but is simulated to access the
     * channel from every instance and be able to perform queries etc
     * without having the need to access the real channel instance
     */
    @Deprecated
    @NotNull
    PacketChannel getSimulatedPacketChannel();

    /**
     * Determines if this service has been registered as a fallback
     * If returning true this will be collected when searching for fallbacks
     */
    boolean isRegisteredAsFallback();

    /**
     * Updates the nameTags for all {@link CloudPlayer}s on this service
     * based on their {@link cloud.hytora.driver.module.permission.PermissionGroup}
     * if the house-own permsModule is enabled
     */
    void updateNametags();

    /**
     * Checks if this {@link CloudService} is running on the provided {@link INode}
     *
     * @param node the node to check
     * @return boolean
     */
    boolean isRunningOn(INode node);

    /**
     * @see ServicePing
     * @return the provided PingOptions for this service
     */
    ServicePing getPingProperties();

    /**
     * Edits the {@link ServicePing} of this service
     * and then calls {@link CloudService#update(PublishingType...)}
     *
     * @param ping the ping consumer handler
     */
    void editPingProperties(Consumer<ServicePing> ping);

    /**
     * Sets the current newest {@link ServiceCycleData}
     *
     * @param data the value to set
     * @see ServiceCycleData
     */
    void setLastCycleData(ServiceCycleData data);

    /**
     * @return the name of the {@link INode} this service runs on
     */
    @NotNull
    String getRunningNodeName();

    /**
     * @return the {@link INode} instance this service runs on
     */
    @NotNull
    INode getRunningNode();

    /**
     * Sets the name of the npde that this service runs on
     *
     * @param name the name of the node
     */
    void setRunningNodeName(String name);

    /**
     * Sets the name of the folder that you want to be set
     * as the default world instance of this service.
     * It internally copies the folder that you provide (e.g. "myCrazyWorld")
     * and copies it to the normal "world" folder to be automatically loaded.
     *
     * @param path the folder name
     */
    void setDefaultWorld(String path);

    /**
     * @return the name of the default world
     * @see CloudService#setDefaultWorld(String)
     */
    String getDefaultWorld();

    /**
     * Deploys this {@link CloudService} into its template
     * using the provided {@link ServiceDeployment}s
     *
     * @param deployments the values
     * @see ServiceDeployment
     */
    void deploy(ServiceDeployment... deployments);

    /**
     * Deploys a single {@link File} of this service to a provided
     * {@link ServiceTemplate} and to a specific destination
     *
     * @param file the file to copy
     * @param template the template to copy to
     * @param destinationPathInTemplate the path in the template
     */
    void deployFile(File file, ServiceTemplate template, String destinationPathInTemplate);

    /**
     * This checks if the service has timed out
     * using the provided {@link CloudService#getLastCycleData()} and the latency
     *
     * @return if timed out
     */
    boolean isTimedOut();

    /**
     * This value is only set to true if this server
     * has fully started up.
     * Meaning connecting,handshaking, registering and requesting data
     * is already done when this is set to true
     *
     * @return if ready
     */
    boolean isReady();

    /**
     * Sets the state of this server to read
     *
     * @param ready if ready
     * @see CloudService#isReady()
     */
    void setReady(boolean ready);

    /**
     * Shuts down this server instance
     * and automatically unregisters it and so on...
     */
    void shutdown();

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

    /**
     * @return a readable uptime of this service
     */
    String getReadableUptime();

    /**
     * @return the timeStamp this service was created
     */
    long getCreationTimestamp();

    /**
     * @return the timeStamp this service was marked as ready
     * @see CloudService#isReady()
     */
    long getReadyTimestamp();

    /**
     * @return the task of the service
     */
    ServiceTask getTask();

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
    @NotNull
    ServiceVisibility getServiceVisibility();

    /**
     * sets the service visibility
     *
     * @param serviceVisibility the service visibility to set
     */
    void setServiceVisibility(@NotNull ServiceVisibility serviceVisibility);

    /**
     * @return the online amount of the service
     */
    @Deprecated
    default int getOnlinePlayerCount() {
        return getOnlinePlayers().size();
    }

    /**
     * @return a {@link Collection} of {@link CloudPlayer}s that are online on this Server
     */
    @NotNull
    Collection<CloudPlayer> getOnlinePlayers();

    /**
     * @return if the service is full
     */
    default boolean isFull() {
        return this.getOnlinePlayerCount() >= this.getMaxPlayers();
    }

    /**
     * The preset motd of this {@link CloudService}
     *
     * @return string motd
     */
    String getMotd();

    /**
     * Sets the motd of this service
     *
     * @param motd the value to set
     */
    void setMotd(String motd);

    /**
     * Sends a command line to this service
     * that will be executed from the server console
     *
     * @param commandLine the line to execute
     */
    void sendCommand(@NotNull String commandLine);

    /**
     * Sends a {@link ChannelMessage} to this service
     *
     * @param message the message to send
     * @see ChannelMessage
     */
    void sendChannelMessage(ChannelMessage message);

    /**
     * Updates this {@link CloudService} either
     * internally, globally or both depending on the {@link PublishingType}
     * that you provide
     *
     * @param type the type to publish
     */
    void update(PublishingType... type);

}
