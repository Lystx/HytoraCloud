package cloud.hytora.driver.services;

import cloud.hytora.common.identification.ModifiableUUIDHolder;
import cloud.hytora.common.task.IPromise;
import cloud.hytora.document.Document;
import cloud.hytora.driver.common.IClusterObject;
import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.networking.IPacketExecutor;
import cloud.hytora.driver.node.INode;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.services.fallback.ICloudFallback;
import cloud.hytora.driver.services.task.IServiceTask;
import cloud.hytora.driver.services.deployment.IDeployment;
import cloud.hytora.driver.services.utils.ServiceState;
import cloud.hytora.driver.services.utils.ServiceVisibility;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * The {@link ICloudServer} describes the minecraft-servers and proxy-servers that are running
 * on different {@link INode}s and grouped by different {@link IServiceTask}s 
 * You can work with the latest {@link IServiceCycleData}, the properties in form of a {@link Document}.
 * But you can also modify the {@link ServiceState} ({@link #setServiceState(ServiceState)})
 * or many other values that you can customize on your own
 *
 * @author Lystx
 * @since SNAPSHOT-1.0
 */
public interface ICloudServer extends IClusterObject<ICloudServer>, NetworkComponent, IPacketExecutor, ModifiableUUIDHolder {

    /**
     * Returns the latest {@link IServiceCycleData} that
     * was sent to the {@link INode} this {@link ICloudServer}
     * is currently running on<br>
     * If no data has been cycled yet, the default value will be returned
     * 
     * @return data never null
     * @see IServiceCycleData
     */
    @NotNull
    IServiceCycleData getLastCycleData();

    /**
     * Sets the latest {@link IServiceCycleData} of this server<br>
     * <b>ATTENTION: </b> Only use if you know what you are doing!
     * 
     * @param data the latest data
     * @see IServiceCycleData
     * @see #getLastCycleData() 
     */
    void setLastCycleData(@NotNull IServiceCycleData data);

    /**
     * Returns if this {@link ICloudServer} is registered as an {@link ICloudFallback}
     * and is usable to fallback players to this server
     */
    boolean isRegisteredAsFallback();

    /**
     * Returns the {@link IPingProperties} of this server
     * that define the values that will be displayed in the motd when
     * pinging this server
     * 
     * @return properties
     * @see IPingProperties
     */
    @NotNull
    IPingProperties getPingProperties();

    /**
     * Edits the current {@link IPingProperties} of this server
     * The provided {@link Consumer} handles your modification 
     * and then automatically updates the properties
     *
     * @param ping the consumer
     */
    void editPingProperties(@NotNull Consumer<IPingProperties> ping);
    
    /**
     * Returns the temporary properties in form of a {@link Document}
     * Where you can store properties as long as this server is online
     * 
     * @return properties instance
     * @see Document
     */
    @NotNull
    Document getProperties();

    /**
     * Sets the properties of this server
     * 
     * @param properties the properties to set
     * @see #getProperties() 
     * @see Document
     */
    void setProperties(@NotNull Document properties);

    /**
     * Returns the name of the {@link INode} that this {@link ICloudServer} is running on
     */
    @NotNull
    String getRunningNodeName();

    /**
     * Sets the name of the {@link INode} that this server is running on
     * 
     * @param name the name of the node 
     * @see #getRunningNodeName() 
     */
    void setRunningNodeName(@NotNull String name);

    /**
     * Deploys this server with the given {@link IDeployment}s
     * 
     * @param deployments the deployments
     * @see IDeployment
     */
    void deploy(@NotNull IDeployment... deployments);

    /**
     * Checks if this server has timed out based on the latest {@link IServiceCycleData}
     * and its latency 
     */
    boolean isTimedOut();

    /**
     * Returns if the option "ready" of this server is true
     * and if this service is ready to be joined and use completely
     */
    boolean isReady();

    /**
     * Sets the option "ready" of this server<br>
     * <b>ATTENTION: </b> Only use if you know what you are doing!
     * 
     * @param ready the state
     * @see #isReady()
     */
    void setReady(boolean ready);

    /**
     * Returns the number of this service<br>
     * e.g. => Lobby-1 (1 is the ServiceID here)
     * e.g. => BedWars-3 (3 is the ServiceID here)
     */
    int getServiceID();

    /**
     * Returns the port that this server runs on
     */
    int getPort();

    /**
     * Returns the host that this server runs on
     */
    @NotNull 
    String getHostName();

    /**
     * Returns the readable uptime of this service
     * e.g. => 34 sec
     * e.g. => 1:34 min
     * e.g. => 1:34:32 h
     */
    @NotNull
    String getReadableUptime();

    /**
     * Retrieves the {@link IServiceTask} that was used to start this server
     * This can never be null, except you did something wrong and provided a non-existing
     * {@link IServiceTask} for this server that can not be found in cache!
     * 
     * @see IServiceTask
     */
    @NotNull
    IServiceTask getTask();

    /**
     * Returns an {@link IPromise} that might contain an {@link IServiceTask}
     *
     * @see IServiceTask
     */
    @NotNull
    IPromise<IServiceTask> getTaskAsync();

    /**
     * The current {@link ServiceState} of this server
     * that indicates the progress of this server internally
     * 
     * @see ServiceState
     */
    @NotNull 
    ServiceState getServiceState();

    /**
     * Sets the {@link ServiceState} of this server.
     * Remember to update this server using {@link #update()} afterwards
     *
     * @param serviceState the new state
     * @see ServiceState
     */
    void setServiceState(@NotNull ServiceState serviceState);

    /**
     * Returns the maximum players of the service
     */
    int getMaxPlayers();

    /**
     * Sets the maximum amount of players that are allowed
     * to be on this server at the same time
     * 
     * @param max the maximum amount
     */
    void setMaxPlayers(int max);

    /**
     * Returns the {@link ServiceVisibility} of this server
     * 
     * @see ServiceVisibility
     * @see #setServiceVisibility(ServiceVisibility) 
     */
    @NotNull 
    ServiceVisibility getServiceVisibility();

    /**
     * Sets the {@link ServiceVisibility} of this server
     * 
     * @param serviceVisibility the visibility to set
     * @see ServiceVisibility
     * @see #getServiceVisibility() 
     */
    void setServiceVisibility(@NotNull ServiceVisibility serviceVisibility);

    /**
     * Returns a {@link Collection} with all online {@link ICloudPlayer}s
     * that are currently on this server (either proxy or minecraft)
     */
    @NotNull
    Collection<ICloudPlayer> getOnlinePlayers();

    /**
     * Returns if this server is full (online >= max)
     */
    boolean isFull();

    /**
     * The time in millis when this server was created
     */
    long getCreationTimestamp();

    /**
     * Shuts down this server instance
     */
    void shutdown();

    /**
     * Sends a commandLine to this server's process
     * Those can either be cloud or (proxy/spigot) commands
     *
     * @param commandLine the line to execute
     */
    void sendCommand(@NotNull String commandLine);

    /**
     * Updates this {@link ICloudServer} within the whole cluster
     * and sends every participant the update of this server instance
     */
    void update();


}
