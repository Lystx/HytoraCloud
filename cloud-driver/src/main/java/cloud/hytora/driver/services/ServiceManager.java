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
    @NotNull List<ServiceInfo> getAllCachedServices();

    void setAllCachedServices(List<ServiceInfo> allCachedServices);

    void registerService(ServiceInfo service);

    void unregisterService(ServiceInfo service);

    /**
     * gets all services by a group
     *
     * @param serviceGroup the group of the services
     * @return the services of a group
     */
    default List<ServiceInfo> getAllServicesByGroup(@NotNull ServerConfiguration serviceGroup) {
        return this.getAllCachedServices().stream().filter(it -> it.getConfiguration().equals(serviceGroup)).collect(Collectors.toList());
    }

    /**
     * gets all services of a state
     *
     * @param serviceState the state of the services
     * @return the services of a state
     */
    default List<ServiceInfo> getAllServicesByState(@NotNull ServiceState serviceState) {
        return this.getAllCachedServices().stream().filter(it -> it.getServiceState() == serviceState).collect(Collectors.toList());
    }

    default List<ServiceInfo> getAllServicesByEnvironment(@NotNull SpecificDriverEnvironment environment) {
        return this.getAllCachedServices().stream().filter(it -> it.getConfiguration() != null && it.getConfiguration().getParent() != null && it.getConfiguration().getParent().getEnvironment() == environment).collect(Collectors.toList());
    }

    /**
     * gets a service
     *
     * @param name the name of the service
     * @return the service or null when the service does not exist
     */
    @NotNull Optional<ServiceInfo> getService(@NotNull String name);

    /**
     * gets a service
     *
     * @param name the name of the service
     * @return the service or null when the service does not exist
     */
    default @Nullable ServiceInfo getServiceByNameOrNull(@NotNull String name) {
        return this.getService(name).orElse(null);
    }

    List<String> queryServiceOutput(ServiceInfo service);

    /**
     * starts a service
     *
     * @param service the service to start
     */
    Task<ServiceInfo> startService(@NotNull ServiceInfo service);

    @Nonnull
    @CheckReturnValue
    Task<ServiceInfo> getFallbackAsService();

    @Nullable
    default ServiceInfo getFallbackAsServiceOrNull() {
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
    List<ServiceInfo> getAvailableFallbacksAsServices();


    @Nonnull
    @CheckReturnValue
    List<FallbackEntry> getAvailableFallbacks();


    /**
     * update a service
     *
     * @param service the service to start
     */
    void updateService(ServiceInfo service);

    void shutdownService(ServiceInfo service);

    /**
     * send a service a packet
     *
     * @param service the service to start
     * @param packet  the packet to send
     */
    void sendPacketToService(ServiceInfo service, Packet packet);

}
