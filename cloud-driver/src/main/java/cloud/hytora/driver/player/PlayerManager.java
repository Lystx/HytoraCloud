package cloud.hytora.driver.player;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.PublishingType;
import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.driver.services.utils.SpecificDriverEnvironment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

public interface PlayerManager {

    @NotNull Collection<ICloudPlayer> getAllCachedCloudPlayers();


    void setCachedCloudPlayers(Collection<ICloudPlayer> allCachedCloudPlayers);

    @Nullable
    ICloudPlayer getCachedCloudPlayer(@NotNull UUID uniqueId);

    @Nullable
    ICloudPlayer getCachedCloudPlayer(@NotNull String username);


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
        for (ICloudService allCachedService : CloudDriver.getInstance().getServiceManager().getAllServicesByEnvironment(SpecificDriverEnvironment.PROXY)) {
            capacity += allCachedService.getMaxPlayers();
        }
        return capacity;
    }

    /**
     * update a cloud player
     *
     * @param cloudPlayer the unique id of the player
     */
    void updateCloudPlayer(@NotNull ICloudPlayer cloudPlayer, PublishingType... type);

    /**
     * unregisters a cloud player
     *
     * @param uniqueId the unique id of the player
     * @param username the username of the player
     */
    void unregisterCloudPlayer(@NotNull UUID uniqueId, @NotNull String username);

}
