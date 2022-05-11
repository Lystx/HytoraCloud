package cloud.hytora.driver.player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerManager {

    /**
     * @return a list of all cloud players
     */
    @NotNull List<CloudPlayer> getAllCachedCloudPlayers();

    void setAllCachedCloudPlayers(List<CloudPlayer> allCachedCloudPlayers);

    /**
     * @param uniqueId the unique id to get the player
     * @return the player in an optional
     */
    @NotNull Optional<CloudPlayer> getCloudPlayer(@NotNull UUID uniqueId);

    /**
     * @param username the username to get the player
     * @return the player in an optional
     */
    @NotNull Optional<CloudPlayer> getCloudPlayer(@NotNull String username);

    /**
     * @param uniqueId the unique id to get the player
     * @return the player
     */
    @Nullable CloudPlayer getCloudPlayerByUniqueIdOrNull(@NotNull UUID uniqueId);

    /**
     * @param username the username to get the player
     * @return the player
     */
    @Nullable CloudPlayer getCloudPlayerByNameOrNull(@NotNull String username);

    /**
     * @return the online count
     */
    default int getCloudPlayerOnlineAmount() {
        return this.getAllCachedCloudPlayers().size();
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
