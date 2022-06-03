package cloud.hytora.driver.player;

import cloud.hytora.common.wrapper.Wrapper;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.protocol.packets.QueryState;
import cloud.hytora.driver.services.CloudServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerManager {

    @NotNull List<CloudPlayer> getAllCachedCloudPlayers();


    void setAllCachedCloudPlayers(List<CloudPlayer> allCachedCloudPlayers);

    @NotNull
    Optional<CloudPlayer> getCloudPlayer(@NotNull UUID uniqueId);

    @NotNull
    Optional<CloudPlayer> getCloudPlayer(@NotNull String username);

    @Nullable
    CloudPlayer getCloudPlayerByUniqueIdOrNull(@NotNull UUID uniqueId);

    @Nullable
    CloudPlayer getCloudPlayerByNameOrNull(@NotNull String username);

    @NotNull
    Wrapper<Collection<CloudOfflinePlayer>> getAllOfflinePlayersAsync();

    @NotNull
    Collection<CloudOfflinePlayer> getAllOfflinePlayersBlockingOrEmpty();

    @NotNull
    Wrapper<CloudOfflinePlayer> getOfflinePlayerByUniqueIdAsync(@NotNull UUID uniqueId);

    @NotNull
    Wrapper<CloudOfflinePlayer> getOfflinePlayerByNameAsync(@NotNull String name);

    @Nullable
    CloudOfflinePlayer getOfflinePlayerByUniqueIdBlockingOrNull(@NotNull UUID uniqueId);

    @Nullable
    CloudOfflinePlayer getOfflinePlayerByNameBlockingOrNull(@NotNull String name);

    void saveOfflinePlayerAsync(@NotNull CloudOfflinePlayer player);

    /**
     * @return the online count
     */
    default int getCloudPlayerOnlineAmount() {
        return this.getAllCachedCloudPlayers().size();
    }

    default int countPlayerCapacity() {
        int capacity = 0;
        for (CloudServer allCachedService : CloudDriver.getInstance().getServiceManager().getAllCachedServices()) {
            capacity += allCachedService.getMaxPlayers();
        }
        return capacity;
    }

    /**
     * registers a cloud player
     *
     * @param uniqueId the unique id of the player
     * @param username the username of the player
     */
    void registerCloudPlayer(@NotNull UUID uniqueId, @NotNull String username);

    /**
     * update a cloud player
     *
     * @param cloudPlayer the unique id of the player
     */
    void updateCloudPlayer(@NotNull CloudPlayer cloudPlayer);

    /**
     * unregisters a cloud player
     *
     * @param uniqueId the unique id of the player
     * @param username the username of the player
     */
    void unregisterCloudPlayer(@NotNull UUID uniqueId, @NotNull String username);

}
