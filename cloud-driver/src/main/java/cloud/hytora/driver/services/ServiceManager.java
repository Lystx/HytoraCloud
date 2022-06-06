package cloud.hytora.driver.services;


import cloud.hytora.common.wrapper.Task;
import cloud.hytora.driver.networking.protocol.packets.Packet;
import cloud.hytora.driver.services.fallback.FallbackEntry;
import cloud.hytora.driver.services.configuration.ServerConfiguration;
import cloud.hytora.driver.services.utils.ServiceState;

import cloud.hytora.driver.services.utils.SpecificDriverEnvironment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public interface ServiceManager {

    /**
     * @return all cached service
     */
    @NotNull List<CloudServer> getAllCachedServices();

    void setAllCachedServices(List<CloudServer> allCachedServices);

    void registerService(CloudServer service);

    void unregisterService(CloudServer service);

    /**
     * gets all services by a group
     *
     * @param serviceGroup the group of the services
     * @return the services of a group
     */
    default List<CloudServer> getAllServicesByGroup(@NotNull ServerConfiguration serviceGroup) {
        return this.getAllCachedServices().stream().filter(it -> it.getConfiguration().equals(serviceGroup)).collect(Collectors.toList());
    }

    /**
     * gets all services of a state
     *
     * @param serviceState the state of the services
     * @return the services of a state
     */
    default List<CloudServer> getAllServicesByState(@NotNull ServiceState serviceState) {
        return this.getAllCachedServices().stream().filter(it -> it.getServiceState() == serviceState).collect(Collectors.toList());
    }

    default List<CloudServer> getAllServicesByEnvironment(@NotNull SpecificDriverEnvironment environment) {
        return this.getAllCachedServices().stream().filter(it -> it.getConfiguration() != null && it.getConfiguration().getParent() != null && it.getConfiguration().getParent().getEnvironment() == environment).collect(Collectors.toList());
    }

    /**
     * gets a service
     *
     * @param name the name of the service
     * @return the service or null when the service does not exist
     */
    @NotNull Optional<CloudServer> getService(@NotNull String name);

    /**
     * gets a service
     *
     * @param name the name of the service
     * @return the service or null when the service does not exist
     */
    default @Nullable CloudServer getServiceByNameOrNull(@NotNull String name) {
        return this.getService(name).orElse(null);
    }

    List<String> queryServiceOutput(CloudServer service);

    /**
     * starts a service
     *
     * @param service the service to start
     */
    Task<CloudServer> startService(@NotNull CloudServer service);

    @Nonnull
    @CheckReturnValue
    Task<CloudServer> getFallbackAsService();

    @Nullable
    default CloudServer getFallbackAsServiceOrNull() {
        return getFallbackAsService().get();
    }

    @Nonnull
    @CheckReturnValue
    Task<FallbackEntry> getFallbackAsEntry();

    @Nullable
    default FallbackEntry getFallbackAsEntryOrNull() {
        return getFallbackAsEntry().get();
    }
    @Nonnull
    @CheckReturnValue
    List<CloudServer> getAvailableFallbacksAsServices();


    @Nonnull
    @CheckReturnValue
    List<FallbackEntry> getAvailableFallbacks();


    /**
     * update a service
     *
     * @param service the service to start
     */
    void updateService(CloudServer service);

    void shutdownService(CloudServer service);

    /**
     * send a service a packet
     *
     * @param service the service to start
     * @param packet  the packet to send
     */
    void sendPacketToService(CloudServer service, Packet packet);

}
