package cloud.hytora.driver.entity.services;


import cloud.hytora.common.task.Task;
import cloud.hytora.driver.common.PublishingType;
import cloud.hytora.driver.common.exception.IncompatibleDriverEnvironmentException;
import cloud.hytora.driver.entity.services.task.ServiceTaskManager;
import cloud.hytora.driver.entity.services.task.bundle.TaskGroup;
import cloud.hytora.driver.networking.protocol.packets.IPacket;
import cloud.hytora.driver.entity.player.CloudPlayer;
import cloud.hytora.driver.entity.services.fallback.FallbackEntry;
import cloud.hytora.driver.entity.services.task.ServiceTask;
import cloud.hytora.driver.entity.services.utils.ServiceState;

import cloud.hytora.driver.entity.services.utils.SpecificDriverEnvironment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The {@link ServiceTaskManager} is used to manage all {@link CloudService}s
 * It is also responsible internally for starting/stopping services and processing
 *
 * @see ServiceTask
 * @see TaskGroup
 *
 * @author Lystx
 * @since DEV-1.3
 * @version STABLE-2.0
 */
public interface ServiceManager {

    /**
     * Returns a {@link List} of all currently cached {@link CloudService}s
     * of your Driver Instance without filtering any kind of services
     *
     * @see #setAllCachedServices(List)
     */
    @NotNull
    List<CloudService> getAllCachedServices();

    /**
     * Public method to override ALL the currently cached {@link CloudService}
     * <br><br>
     *
     * <b>ATTENTION:</b> Only use this method if you know what you are doing!
     * @param allCachedServices the services to set
     * @see #getAllCachedServices()
     */
    void setAllCachedServices(List<CloudService> allCachedServices);

    /**
     * Registers a given {@link CloudService} into the cache
     * of the current Driver Instance
     * <br>
     * If somehow this {@link CloudService} is already registered on the Node-Side
     * simply nothing will happen and the service won't be registered twice.
     *
     * @param service the service to register
     */
    void registerService(CloudService service);

    /**
     * Tries to update down a given {@link CloudService}
     * <br>
     * If this {@link CloudService} is not registered for any reason
     * simply nothing will happen and the method just returns
     *
     * @param service the service to update
     */
    void updateService(CloudService service, PublishingType... type);

    /**
     * Tries to unregister a given {@link CloudService}
     * <br>
     * If somehow this {@link CloudService} has not been registered on the Node-Side before
     * simply nothing will happen and the service won't be unregistered.
     *
     * @param service the service to unregister
     */
    void unregisterService(CloudService service);

    /**
     * Tries to shut down a given {@link CloudService}
     * <br>
     * If this {@link CloudService} is not registered for any reason
     * simply nothing will happen and the method just returns
     *
     * @param service the service to stop
     */
    void shutdownService(CloudService service);

    /**
     * Retrieves a {@link List} of all {@link CloudService}s
     * that match the provided {@link ServiceTask}
     *
     * @param serviceTask the task to check
     * @return list of found services
     */
    @NotNull
    List<CloudService> getAllServicesByTask(@NotNull ServiceTask serviceTask);

    /**
     * Retrieves a {@link List} of all {@link CloudService}s
     * that match the provided {@link ServiceState}
     *
     * @param serviceState the state to check
     * @return list of found services
     */
    List<CloudService> getAllServicesByState(@NotNull ServiceState serviceState);

    /**
     * Retrieves a {@link List} of all {@link CloudService}s
     * that match the provided {@link SpecificDriverEnvironment}
     *
     * @param environment the environment to check
     * @return list of found services
     */
    List<CloudService> getAllServicesByEnvironment(@NotNull SpecificDriverEnvironment environment);

    /**
     * Retrieves a cached {@link CloudService} by its name
     * if it is cached internally
     * This method is not case-sensitive
     *
     * @param name the name of the server
     * @return the found server or null (if not found)
     */
    CloudService getCachedCloudService(@NotNull String name);

    /**
     * Creates a new {@link Task} that is responsible
     * for starting the provided {@link CloudService}
     *
     * The task is completed when the {@link CloudService} is fully started
     * and has been set to be ready {@link CloudService#isReady()}
     *
     * @param service the service to start
     * @return the task instance
     */
    @NotNull
    Task<CloudService> startService(@NotNull CloudService service);

    /**
     * Tries to find a {@link CloudService} instance that has
     * been registered as a {@link FallbackEntry}
     *
     * @param player the player to find a fallback for
     * @return the found instance or null (if maybe not fallback online)
     */
    @Nullable
    CloudService findFallback(CloudPlayer player);

    /**
     * Creates a new {@link Task} that searches for a {@link CloudService}
     * that has been registered as a {@link FallbackEntry}
     *
     * This method uses {@link #getAvailableFallbacksAsServices()}
     * and sorts it to the fewest players so all the fallbacks are mostly balanced
     * The task is completed when a fallback is found
     *
     * @return created task
     */
    @Nonnull
    Task<CloudService> getFallbackAsService();

    /**
     * Searches for every {@link FallbackEntry} registered
     * and checks for following criteria:
     *
     * => State: ONLINE
     * => Visibility: VISIBLE
     * => Version != PROXY
     *
     * and then sorts it descending of player amount
     * and then parses it to {@link CloudService}s
     *
     * @return the found instances
     */
    @Nonnull
    List<CloudService> getAvailableFallbacksAsServices();

    /**
     * Gets the current {@link CloudService} that this instance is running on
     *
     * @return found instance
     * @throws IncompatibleDriverEnvironmentException if executed anywhere than Service
     */
    CloudService thisService() throws IncompatibleDriverEnvironmentException;

    /**
     * Sends a {@link IPacket} to a given {@link CloudService}
     * If this method is executed on Node-Side it will instantly search
     * for the provided service channel and flush the packet into it<br><br>
     *
     * But if executed on Remote-Side this method will send a Packet to the Node<br>
     * and when this packet is being handled it does the above described action.
     *
     * @param service the service to send the packet to
     * @param packet the packet to send
     */
    void sendPacketToService(CloudService service, IPacket packet);

}
