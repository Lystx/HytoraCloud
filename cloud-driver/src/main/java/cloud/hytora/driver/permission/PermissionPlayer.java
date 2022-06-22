package cloud.hytora.driver.permission;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.player.CloudOfflinePlayer;
import cloud.hytora.driver.player.CloudPlayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * This class represents a Database entry for a specific {@link CloudPlayer}
 * That is being put all together into a {@link PermissionPlayer} to access
 * this data set and add data to this player and later on save it to the database again
 *
 * @author Lystx
 * @since SNAPSHOT-1.3
 */
public interface PermissionPlayer extends PermissionEntity {


	static PermissionPlayer ofPlayer(CloudOfflinePlayer player) {
		return CloudDriver.getInstance().getProviderRegistry().get(PermissionManager.class).mapOrElse(pm -> pm.getPlayer(player), () -> null);
	}

	static PermissionPlayer ofOfflineByName(String name) {
		return ofPlayer(CloudDriver.getInstance().getPlayerManager().getOfflinePlayerByNameBlockingOrNull(name));
	}

	static PermissionPlayer ofOnlineByName(String name) {
		return ofPlayer(CloudDriver.getInstance().getPlayerManager().getCloudPlayerByNameOrNull(name));
	}

	static PermissionPlayer ofOfflineByUniqueId(UUID uniqueID) {
		return ofPlayer(CloudDriver.getInstance().getPlayerManager().getOfflinePlayerByUniqueIdBlockingOrNull(uniqueID));
	}

	static PermissionPlayer ofOnlineByUniqueID(UUID uniqueID) {
		return ofPlayer(CloudDriver.getInstance().getPlayerManager().getCloudPlayerByUniqueIdOrNull(uniqueID));
	}

	/**
	 * Tries to get the {@link CloudPlayer} player of this permission
	 * database entry<br>
	 *
	 * @return the player if online -> otherwise null
	 */
	@Nullable
	CloudPlayer toOnlinePlayer();

	/**
	 * Tries to get the {@link CloudOfflinePlayer} player of this permission
	 * database entry<br>
	 *
	 * @return the player if found in database
	 */
	@Nonnull
	CloudOfflinePlayer toOfflinePlayer();

	/**
	 * Returns every {@link PermissionGroup} instance that this player owns
	 * and that have not expired yet
	 */
	@Nonnull
	Collection<PermissionGroup> getPermissionGroups();

	/**
	 * Returns the highest {@link PermissionGroup} sorted by {@link PermissionGroup#getSortId()}<br>
	 * Might be null if the player is in no groups
	 *
	 * @return group instance or null
	 */
	@Nullable
	PermissionGroup getHighestGroup();

	/**
	 * Adds the provided {@link PermissionGroup} to the data of this player permanently<br>
	 * If the player is already in this group nothing will happen
	 * <br><br>
	 * @param group the group to add
	 */
	void addPermissionGroup(@Nonnull PermissionGroup group);

	/**
	 * Adds the provided {@link PermissionGroup} to the data of this player for a temporary time<br>
	 * If the player is already in this group nothing will happen
	 * <br><br>
	 * @param group the group to add
	 * @param unit the unit for the timeOut of this group
	 * @param value the value for the unit
	 */
	void addPermissionGroup(@Nonnull PermissionGroup group, TimeUnit unit, long value);

	/**
	 * Tries to remove a certain {@link PermissionGroup} from this player if he owns it<br>
	 * If this player is not in the provided group nothing will happen
	 * <br><br>
	 * @param groupName the name of the group
	 */
	void removePermissionGroup(String groupName);
}
