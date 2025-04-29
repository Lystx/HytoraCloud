package cloud.hytora.driver.services;


import cloud.hytora.common.task.Task;
import cloud.hytora.driver.PublishingType;
import cloud.hytora.driver.networking.protocol.packets.IPacket;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.services.fallback.FallbackEntry;
import cloud.hytora.driver.services.task.IServiceTask;
import cloud.hytora.driver.services.utils.ServiceState;

import cloud.hytora.driver.services.utils.SpecificDriverEnvironment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

// TODO: 04.08.2022 rework documentation
public interface ServiceManager {

    /**
     * Returns a {@link List} of all currently cached {@link ICloudService}s
     * of your Driver Instance without filtering any kind of services
     *
     * @see #setAllCachedServices(List)
     */
    @NotNull
    List<ICloudService> getAllCachedServices();

    /**
     * Public method to override ALL the currently cached {@link ICloudService}
     * <br><br>
     *
     * <b>ATTENTION:</b> Only use this method if you know what you are doing!
     * @param allCachedServices the services to set
     * @see #getAllCachedServices()
     */
    void setAllCachedServices(List<ICloudService> allCachedServices);

    /**
     * Registers a given {@link ICloudService} into the cache
     * of the current Driver Instance
     * <br>
     * If somehow this {@link ICloudService} is already registered on the Node-Side
     * simply nothing will happen and the service won't be registered twice.
     *
     * @param service the service to register
     */
    void registerService(ICloudService service);

    /**
     * Tries to update down a given {@link ICloudService}
     * <br>
     * If this {@link ICloudService} is not registered for any reason
     * simply nothing will happen and the method just returns
     *
     * @param service the service to update
     */
    void updateService(ICloudService service, PublishingType... type);

    /**
     * Tries to unregister a given {@link ICloudService}
     * <br>
     * If somehow this {@link ICloudService} has not been registered on the Node-Side before
     * simply nothing will happen and the service won't be unregistered.
     *
     * @param service the service to unregister
     */
    void unregisterService(ICloudService service);

    /**
     * Tries to shut down a given {@link ICloudService}
     * <br>
     * If this {@link ICloudService} is not registered for any reason
     * simply nothing will happen and the method just returns
     *
     * @param service the service to stop
     */
    void shutdownService(ICloudService service);

    @Nullable
    ICloudService findFallback(ICloudPlayer player);


    default List<ICloudService> getAllServicesByTask(@NotNull IServiceTask serviceTask) {
        if (serviceTask == null) {
            return new ArrayList<>();
        }
        return this.getAllCachedServices()
                .stream()
                .filter(Objects::nonNull)
                .filter(it -> it.getTask() != null)
                .filter(it -> it.getTask().getName().equalsIgnoreCase(serviceTask.getName()))
                .collect(Collectors.toList());
    }

    default List<ICloudService> getAllServicesByState(@NotNull ServiceState serviceState) {
        return this.getAllCachedServices().stream().filter(it -> it.getServiceState() == serviceState).collect(Collectors.toList());
    }

    default List<ICloudService> getAllServicesByEnvironment(@NotNull SpecificDriverEnvironment environment) {
        return this.getAllCachedServices().stream().filter(it -> it.getTask() != null && it.getTask().getTaskGroup() != null && it.getTask().getTaskGroup().getEnvironment() == environment).collect(Collectors.toList());
    }

    @NotNull
    Task<ICloudService> getCloudService(@NotNull String name);

    ICloudService getCachedCloudService(@NotNull String name);

    Task<ICloudService> startService(@NotNull ICloudService service);

    @Nonnull
    Task<ICloudService> getFallbackAsService();

    Task<ICloudService> getThisService();

    ICloudService thisService();


    @Nonnull
    Task<FallbackEntry> getFallbackAsEntry();

    @Nonnull
    List<ICloudService> getAvailableFallbacksAsServices();

    @Nonnull
    List<FallbackEntry> getAvailableFallbacks();

    /**
     * Sends a {@link Packet} to a given {@link ICloudService}
     * If this method is executed on Node-Side it will instantly search
     * for the provided service channel and flush the packet into it<br><br>
     *
     * But if executed on Remote-Side this method will send a Packet to the Node<br>
     * and when this packet is being handled it does the above described action.
     *
     * @param service the service to send the packet to
     * @param packet the packet to send
     */
    void sendPacketToService(ICloudService service, IPacket packet);

}
