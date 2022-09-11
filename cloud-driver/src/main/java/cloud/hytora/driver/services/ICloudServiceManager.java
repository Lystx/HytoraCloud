package cloud.hytora.driver.services;


import cloud.hytora.common.task.IPromise;
import cloud.hytora.driver.networking.protocol.packets.IPacket;
import cloud.hytora.driver.services.fallback.ICloudFallback;
import cloud.hytora.driver.services.task.IServiceTask;
import cloud.hytora.driver.services.utils.ServiceState;

import cloud.hytora.driver.services.utils.SpecificDriverEnvironment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The {@link ICloudServiceManager} manages all the currently existing {@link ICloudServer}s
 * You can start or stop a certain {@link ICloudServer} and filter for your {@link ICloudServer}
 * or {@link ICloudFallback}
 *
 * @author Lystx
 * @since SNAPSHOT-1.0
 */
public interface ICloudServiceManager {

    /**
     * Returns a {@link List} of all currently cached {@link ICloudServer}s
     * of your Driver Instance without filtering any kind of services
     *
     * @see #setAllCachedServices(List)
     */
    @NotNull
    Collection<ICloudServer> getAllCachedServices();

    /**
     * Public method to override ALL the currently cached {@link ICloudServer}
     * <br><br>
     *
     * <b>ATTENTION:</b> Only use this method if you know what you are doing!
     * @param allCachedServices the services to set
     * @see #getAllCachedServices()
     */
    void setAllCachedServices(@NotNull List<ICloudServer> allCachedServices);

    /**
     * Registers a given {@link ICloudServer} into the cache
     * of the current Driver Instance
     * <br>
     * If somehow this {@link ICloudServer} is already registered on the Node-Side
     * simply nothing will happen and the service won't be registered twice.
     *
     * @param service the service to register
     */
    void registerService(@NotNull ICloudServer service);

    /**
     * Tries to updateTask down a given {@link ICloudServer}
     * <br>
     * If this {@link ICloudServer} is not registered for any reason
     * simply nothing will happen and the method just returns
     *
     * @param service the service to updateTask
     */
    void updateService(@NotNull ICloudServer service);

    /**
     * Tries to unregister a given {@link ICloudServer}
     * <br>
     * If somehow this {@link ICloudServer} has not been registered on the Node-Side before
     * simply nothing will happen and the service won't be unregistered.
     *
     * @param service the service to unregister
     */
    void unregisterService(@NotNull ICloudServer service);

    /**
     * Tries to shut down a given {@link ICloudServer}
     * <br>
     * If this {@link ICloudServer} is not registered for any reason
     * simply nothing will happen and the method just returns
     *
     * @param service the service to stop
     */
    void shutdownService(@NotNull ICloudServer service);

    /**
     * Returns a {@link Collection} of all {@link ICloudServer}s that
     * share the same {@link IServiceTask} as a parent
     *
     * @param serviceTask the task that should match
     * @return modifiable collection
     */
    @NotNull
    default Collection<ICloudServer> getAllServicesByTask(@NotNull IServiceTask serviceTask) {
        return this.getAllCachedServices()
                .stream()
                .filter(it -> it.getTask() != null)
                .filter(it -> it.getTask().getName().equalsIgnoreCase(serviceTask.getName()))
                .collect(Collectors.toList());
    }

    /**
     * Returns a {@link Collection} of all {@link ICloudServer}s that
     * share the same {@link ServiceState} currently
     *
     * @param serviceState the state that should match
     * @return modifiable collection
     */
    @NotNull
    default Collection<ICloudServer> getAllServicesByState(@NotNull ServiceState serviceState) {
        return this.getAllCachedServices()
                .stream()
                .filter(it -> it.getServiceState() == serviceState)
                .collect(Collectors.toList());
    }

    /**
     * Returns a {@link Collection} of all {@link ICloudServer}s that
     * share the same {@link SpecificDriverEnvironment} currently
     *
     * @param environment the environment that should match
     * @return modifiable collection
     */
    @NotNull
    default List<ICloudServer> getAllServicesByEnvironment(@NotNull SpecificDriverEnvironment environment) {
        return this.getAllCachedServices().stream().filter(it -> it.getTask() != null && it.getTask().getTaskGroup() != null && it.getTask().getTaskGroup().getEnvironment() == environment).collect(Collectors.toList());
    }

    /**
     * Returns an {@link IPromise} that asynchronously returns the
     * {@link ICloudServer} that matches the provided name
     *
     * @param name the name of the server
     * @return task instance
     */
    @NotNull
    IPromise<ICloudServer> getServiceAsync(@NotNull String name);

    /**
     * Tries to return the {@link ICloudServer} that matches
     * the provided name.
     * Will return if no {@link ICloudServer} with that name is cached
     *
     * @param name the name to match
     * @return server instance or null
     */
    @Nullable
    ICloudServer getService(@NotNull String name);

    @Nonnull
    IPromise<ICloudServer> startService(@NotNull ICloudServer service);

    /**
     * Returns an {@link IPromise} that might contain the current {@link ICloudServer}
     *
     * @see #thisServiceOrNull()
     */
    @Nonnull
    IPromise<ICloudServer> thisService();

    /**
     * Returns the {@link ICloudServer} that belongs to this Driver-Instance.
     * If not executed on Server-Side it will return null
     *
     * @return server or null
     */
    @Nullable
    ICloudServer thisServiceOrNull();

    /**
     * Returns an {@link IPromise} that might contain the current
     * {@link ICloudFallback} using {@link #getFallbackAsEntry()}
     * and finding the perfect {@link ICloudServer} for it
     *
     * @return task instance
     */
    @Nonnull
    IPromise<ICloudServer> getFallbackAsService();

    /**
     * Returns the value from {@link #getFallbackAsService()}
     *
     * @return server instance or null
     */
    @Nullable
    default ICloudServer getFallbackAsServiceOrNull() {
        return getFallbackAsService().get();
    }

    /**
     * Retrieves an {@link IPromise} containing the
     * default {@link ICloudFallback} that you can work with
     *
     * @see #getFallbackAsEntryOrNull()
     */
    @Nonnull
    IPromise<ICloudFallback> getFallbackAsEntry();

    /**
     * Retrieves the default {@link ICloudFallback} that is available
     */
    @Nullable
    default ICloudFallback getFallbackAsEntryOrNull() {
        return getFallbackAsEntry().orElse(null);
    }

    /**
     * Returns a {@link Collection} of all available {@link ICloudFallback}s
     * but maps all these available fallbacks to {@link ICloudServer}s
     */
    @Nonnull
    Collection<ICloudServer> getAvailableFallbacksAsServices();

    /**
     * Retrieves a {@link java.util.Collection} of {@link ICloudFallback}s
     * that are available (not excluding certain {@link ICloudFallback}s due to their
     * accessibility permissions or sorting-orders)
     */
    @Nonnull
    Collection<ICloudFallback> getAvailableFallbacks();

    /**
     * Sends a {@link IPacket} to a given {@link ICloudServer}
     * If this method is executed on Node-Side it will instantly search
     * for the provided service channel and flush the packet into it<br><br>
     *
     * But if executed on Remote-Side this method will send a Packet to the Node<br>
     * and when this packet is being handled it does the above described action.
     *
     * @param service the service to send the packet to
     * @param packet the packet to send
     */
    void sendPacketToService(@NotNull ICloudServer service, @NotNull IPacket packet);

}
