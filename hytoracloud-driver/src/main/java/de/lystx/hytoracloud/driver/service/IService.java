package de.lystx.hytoracloud.driver.service;

import de.lystx.hytoracloud.driver.config.impl.proxy.Motd;
import de.lystx.hytoracloud.driver.connection.protocol.netty.global.packet.impl.response.ResponseStatus;
import de.lystx.hytoracloud.driver.connection.protocol.netty.global.packet.IPacket;
import de.lystx.hytoracloud.driver.service.minecraft.plugin.PluginInfo;
import de.lystx.hytoracloud.driver.service.receiver.IReceiver;
import de.lystx.hytoracloud.driver.utils.enums.cloud.ServiceState;
import de.lystx.hytoracloud.driver.utils.interfaces.Identifiable;
import de.lystx.hytoracloud.driver.utils.interfaces.Syncable;
import de.lystx.hytoracloud.driver.player.ICloudPlayer;
import de.lystx.hytoracloud.driver.connection.protocol.requests.base.DriverQuery;
import de.lystx.hytoracloud.driver.utils.json.JsonObject;
import de.lystx.hytoracloud.driver.utils.json.PropertyObject;
import de.lystx.hytoracloud.driver.service.group.IServiceGroup;


import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;


public interface IService extends Serializable, Syncable<IService>, Identifiable {

    /**
     * Sends a {@link IPacket} to only this {@link IService}
     *
     * @param packet the packet
     */
    void sendPacket(IPacket packet);

    /**
     * Sets the host of this service
     *
     * @param host the host
     */
    DriverQuery<ResponseStatus> setHost(String host);

    /**
     * Sets the host of this service
     * but does not update it directly
     *
     * @param host the host
     */
    void setCachedHost(String host);

    /**
     * Verifies this whole service and updates all values
     *
     * @param host the host
     * @param verified if its authenticated
     * @param state the state
     * @param properties the properties
     * @return query with updated service
     */
    DriverQuery<IService> verify(String host, boolean verified, ServiceState state, JsonObject<?> properties);

    /**
     * Sets the state of this service
     *
     * @param state the state
     */
    DriverQuery<ResponseStatus> setState(ServiceState state);

    /**
     * Sets the state of this service
     * but does not update it directly
     *
     * @param state the state
     */
    void setCachedState(ServiceState state);

    /**
     * Sets the properties of this service
     *
     * @param properties the properties
     */
    DriverQuery<ResponseStatus> setProperties(JsonObject<?> properties);

    /**
     * Sets the properties of this service
     * but does not update it directly
     *
     * @param properties the properties
     */
    void setCachedProperties(JsonObject<?> properties);

    /**
     * The group of this service
     */
    IServiceGroup getGroup();

    /**
     * The group of this service
     * sync with the cloud cache
     *
     * @return optional
     */
    Optional<IServiceGroup> getSyncedGroup();

    /**
     * Sets the group of this service
     *
     * @param serviceGroup the group
     */
    void setGroup(IServiceGroup serviceGroup);

    /**
     * Marks this service as registered
     *
     * @param authenticated boolean
     */
    DriverQuery<ResponseStatus> setAuthenticated(boolean authenticated);

    /**
     * Sets the authentication-state of this service
     * but does not update it directly
     *
     * @param authenticated if authenticated
     */
    void setCachedAuthenticated(boolean authenticated);

    /**
     * Adds a property to this service
     *
     * @param key the name of the property
     * @param propertyObject the propertyObject
     */
    DriverQuery<ResponseStatus> addProperty(String key, JsonObject<?> propertyObject);

    /**
     * Gives you a property object
     * full of information
     *
     * @return properties
     */
    DriverQuery<PropertyObject> requestInfo();

    /**
     * Sets the motd of this service
     *
     * @param motd the motd
     */
    @Deprecated
    DriverQuery<ResponseStatus> setMotd(String motd);

    /**
     * Sets the maxPlayers of this service
     *
     * @param maxPlayers the maxPlayers
     */
    DriverQuery<ResponseStatus> setMaxPlayers(int maxPlayers);

    /**
     * Sets the motd of this service
     *
     * @param motd the motd
     */
    DriverQuery<ResponseStatus> setMotd(Motd motd);

    /**
     * Updates all values of this service
     *
     * @param serviceInfo the info
     * @return response
     */
    DriverQuery<ResponseStatus> setInfo(ServiceInfo serviceInfo);

    /**
     * Gets the formatted tps of this
     * minecraft server
     *
     * @return tps in string with color
     */
    DriverQuery<String> getTPS();

    /**
     * Uploads the log of the server
     * to hastebin and returns the url
     * to view the content online
     *
     * @return query with url link of log
     */
    DriverQuery<String> getLogUrl();

    /**
     * Gets the usage of the service
     *
     * @return memory as query
     */
    DriverQuery<Long> getMemoryUsage();

    /**
     * Returns the Maximum PLayers of this Service
     * might lag if the Service has not been
     * pinged before
     *
     * @return Maximum PLayers of service
     */
    int getMaxPlayers();

    /**
     * Returns the {@link ICloudPlayer}s on this
     * Service (for example "Lobby-1")
     *
     * @return List of cloudPlayers on this service
     */
    List<ICloudPlayer> getPlayers();

    /**
     * Returns the Motd of this Service
     * might lag if the Service has not been
     * pinged before
     *
     * @return Motd of service
     */
    String getMotd();

    /**
     * The properties of this service to store values
     */
    JsonObject<?> getProperties();

    /**
     * Loads a list of {@link PluginInfo}s
     * on this service
     *
     * @return list of plugins
     */
    PluginInfo[] getPlugins();

    /**
     * If the service is connected to the cloud
     */
    boolean isAuthenticated();

    /**
     * The state of this service
     */
    ServiceState getState();

    /**
     * The ID of this service
     */
    int getId();

    /**
     * Gets the {@link IReceiver} of this service
     *
     * @return receiver or null
     */
    IReceiver getReceiver();

    /**
     * Creates an {@link InetSocketAddress}
     * from the host and port of this service
     *
     * @return address
     */
    InetSocketAddress getAddress();

    /**
     * The port of this service
     */
    int getPort();

    /**
     * The host of the cloud to connect to
     */
    String getHost();

    /**
     * Updates this Service
     * and syncs it all over the cloud
     */
    void update();

    /**
     * Stops this service
     */
    void shutdown();

    /**
     * Starts this service
     */
    void bootstrap();

}
