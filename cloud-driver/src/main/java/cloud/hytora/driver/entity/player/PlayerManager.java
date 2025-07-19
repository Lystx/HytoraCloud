package cloud.hytora.driver.entity.player;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.PublishingType;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.entity.services.utils.SpecificDriverEnvironment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

public interface PlayerManager {

    @NotNull Collection<CloudPlayer> getAllCachedCloudPlayers();
    void setCachedCloudPlayers(Collection<CloudPlayer> allCachedCloudPlayers);

    @Nullable
    CloudPlayer getCachedCloudPlayer(@NotNull UUID uniqueId);

    @Nullable
    CloudPlayer getCachedCloudPlayer(@NotNull String username);


    /**
     * Tries to get a {@link CloudOfflinePlayer} from cache.
     * But if it does not exist, it will load it into cache using
     * a query via {@link #getOfflinePlayer(String)}
     *
     * @param name the name of the player
     * @return found or loaded player instance
     */
    CloudOfflinePlayer getCachedOfflinePlayerOrRefresh(@NotNull String name);


    /**
     * Tries to get a {@link CloudOfflinePlayer} from cache.
     * But if it does not exist, it will load it into cache using
     * a query via {@link #getOfflinePlayer(UUID)}
     *
     * @param uniqueId the uuid of the player
     * @return found or loaded player instance
     */
    CloudOfflinePlayer getCachedOfflinePlayerOrRefresh(@NotNull UUID uniqueId);

    /**
     * @return the current local cache of offline players
     */
    @NotNull
    Collection<CloudOfflinePlayer> getCachedOfflinePlayers();

    @NotNull
    Task<Collection<CloudOfflinePlayer>> getOfflinePlayers();

    @NotNull
    Task<CloudOfflinePlayer> getOfflinePlayer(@NotNull UUID uniqueId);

    @NotNull
    Task<CloudOfflinePlayer> getOfflinePlayer(@NotNull String name);

    Task<Void> saveOfflinePlayer(@NotNull CloudOfflinePlayer player);

    /**
     * @return the online count
     */
    default int getCloudPlayerOnlineAmount() {
        return this.getAllCachedCloudPlayers().size();
    }

    default int countPlayerCapacity() {
        int capacity = 0;
        for (CloudService allCachedService : CloudDriver.getInstance().getServiceManager().getAllServicesByEnvironment(SpecificDriverEnvironment.PROXY)) {
            capacity += allCachedService.getMaxPlayers();
        }
        return capacity;
    }

    /**
     * update a cloud player
     *
     * @param cloudPlayer the unique id of the player
     */
    void updateCloudPlayer(@NotNull CloudPlayer cloudPlayer, PublishingType... type);

    /**
     * unregisters a cloud player
     *
     * @param uniqueId the unique id of the player
     * @param username the username of the player
     */
    void unregisterCloudPlayer(@NotNull UUID uniqueId, @NotNull String username);

}
