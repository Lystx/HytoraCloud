package cloud.hytora.driver.services;


import cloud.hytora.common.task.Task;
import cloud.hytora.driver.networking.protocol.packets.IPacket;
import cloud.hytora.driver.services.fallback.FallbackEntry;
import cloud.hytora.driver.services.task.ServiceTask;
import cloud.hytora.driver.services.utils.ServiceState;

import cloud.hytora.driver.services.utils.SpecificDriverEnvironment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public interface ServiceManager {

    /**
     * Returns a {@link List} of all currently cached {@link ICloudServer}s
     * of your Driver Instance without filtering any kind of services
     *
     * @see #setAllCachedServices(List)
     */
    @NotNull
    List<ICloudServer> getAllCachedServices();

    /**
     * Public method to override ALL the currently cached {@link ICloudServer}
     * <br><br>
     *
     * <b>ATTENTION:</b> Only use this method if you know what you are doing!
     * @param allCachedServices the services to set
     * @see #getAllCachedServices()
     */
    void setAllCachedServices(List<ICloudServer> allCachedServices);

    /**
     * Registers a given {@link ICloudServer} into the cache
     * of the current Driver Instance
     * <br>
     * If somehow this {@link ICloudServer} is already registered on the Node-Side
     * simply nothing will happen and the service won't be registered twice.
     *
     * @param service the service to register
     */
    void registerService(ICloudServer service);

    /**
     * Tries to update down a given {@link ICloudServer}
     * <br>
     * If this {@link ICloudServer} is not registered for any reason
     * simply nothing will happen and the method just returns
     *
     * @param service the service to update
     */
    void updateService(ICloudServer service);

    /**
     * Tries to unregister a given {@link ICloudServer}
     * <br>
     * If somehow this {@link ICloudServer} has not been registered on the Node-Side before
     * simply nothing will happen and the service won't be unregistered.
     *
     * @param service the service to unregister
     */
    void unregisterService(ICloudServer service);

    /**
     * Tries to shut down a given {@link ICloudServer}
     * <br>
     * If this {@link ICloudServer} is not registered for any reason
     * simply nothing will happen and the method just returns
     *
     * @param service the service to stop
     */
    void shutdownService(ICloudServer service);


    default List<ICloudServer> getAllServicesByTask(@NotNull ServiceTask serviceTask) {
        return this.getAllCachedServices().stream().filter(it -> it.getTask().getName().equalsIgnoreCase(serviceTask.getName())).collect(Collectors.toList());
    }

    default List<ICloudServer> getAllServicesByState(@NotNull ServiceState serviceState) {
        return this.getAllCachedServices().stream().filter(it -> it.getServiceState() == serviceState).collect(Collectors.toList());
    }

    default List<ICloudServer> getAllServicesByEnvironment(@NotNull SpecificDriverEnvironment environment) {
        return this.getAllCachedServices().stream().filter(it -> it.getTask() != null && it.getTask().getTaskGroup() != null && it.getTask().getTaskGroup().getEnvironment() == environment).collect(Collectors.toList());
    }

    @NotNull
    Optional<ICloudServer> getService(@NotNull String name);

    @Nullable
    default ICloudServer getServiceByNameOrNull(@NotNull String name) {
        return this.getService(name).orElse(null);
    }

    List<String> getScreenOutput(ICloudServer service);

    Task<ICloudServer> startService(@NotNull ICloudServer service);

    @Nonnull
    Task<ICloudServer> getFallbackAsService();

    Task<ICloudServer> thisService();

    ICloudServer thisServiceOrNull();

    @Nullable
    default ICloudServer getFallbackAsServiceOrNull() {
        return getFallbackAsService().get();
    }

    @Nonnull
    Task<FallbackEntry> getFallbackAsEntry();

    @Nullable
    default FallbackEntry getFallbackAsEntryOrNull() {
        return getFallbackAsEntry().get();
    }

    @Nonnull
    List<ICloudServer> getAvailableFallbacksAsServices();

    @Nonnull
    List<FallbackEntry> getAvailableFallbacks();

    /**
     * Sends a {@link Packet} to a given {@link ICloudServer}
     * If this method is executed on Node-Side it will instantly search
     * for the provided service channel and flush the packet into it<br><br>
     *
     * But if executed on Remote-Side this method will send a Packet to the Node<br>
     * and when this packet is being handled it does the above described action.
     *
     * @param service the service to send the packet to
     * @param packet the packet to send
     */
    void sendPacketToService(ICloudServer service, IPacket packet);

}
