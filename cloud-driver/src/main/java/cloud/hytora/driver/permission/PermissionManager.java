package cloud.hytora.driver.permission;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.player.CloudOfflinePlayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


/**
 * The <b>PermissionManager</b> is our way of managing permissions at HytoraCloud.
 * Here you can manage certain {@link PermissionGroup}s, {@link PermissionPlayer}s & {@link Permission}s<br>
 * You can delete and save groups and more.
 *
 * @author Lystx
 * @since SNAPSHOT-1.0
 */
public interface PermissionManager {

	/**
	 * Creates a new instance of a {@link Permission} <br>
	 * Is called when using {@link Permission#of(String, TimeUnit, long)} for example.
	 *
	 * @param permission the name of the permission
	 * @param expirationDate the date when this permission expires (use -1 for permanent)
	 * @see Permission#of(String, TimeUnit, long)
	 * @see Permission#of(String, long)
	 * @see Permission#of(String)
	 * @return created instance
	 */
	@Nonnull
	Permission createPermission(@Nonnull String permission, @Nonnull long expirationDate);

	/**
	 * Creates a new instance of a {@link PermissionGroup}<br>
	 * To modify the values of a group even more you can do so by accessing<br>
	 * the Setter-Methods inside {@link PermissionGroup} after creating this instance
	 * <b>ATTENTION: </b> this method does not save this group it only creates a new instance
	 *
	 * @param name the name of the group
	 * @see PermissionGroup
	 * @return created group instance
	 */
	@Nonnull
	PermissionGroup createPermissionGroup(@Nonnull String name);

	/**
	 * Returns a {@link Collection} of all {@link PermissionGroup}s
	 * that are currently loaded into the cache
	 */
	@Nonnull
	Collection<PermissionGroup> getAllCachedPermissionGroups();

	/**
	 * Retrieves a {@link PermissionGroup} by its name by iterating<br>
	 * through the currently loaded groups from {@link #getAllCachedPermissionGroups()}
	 * <b>ATTENTION: </b> the provided name is not case sensitive
	 *
	 * @param name the name of the group you are trying to find
	 * @return the found instance or null
	 */
	@Nullable
	PermissionGroup getPermissionGroupByNameOrNull(@Nonnull String name);

	/**
	 * Retrieves a {@link PermissionGroup} by its name by iterating<br>
	 * through the currently loaded groups from {@link #getAllCachedPermissionGroups()}
	 * <b>ATTENTION: </b> the provided name is not case sensitive
	 *
	 * @param name the name of the group you are trying to find
	 * @return a task instance that might be holding the found {@link PermissionGroup}
	 */
	@Nonnull
	Task<PermissionGroup> getPermissionGroup(@Nonnull String name);

	/**
	 * Creates a new {@link PermissionPlayer} by using a {@link CloudOfflinePlayer}
	 *
	 * @param player the offline player instance
	 * @return created player instance
	 */
	@Nonnull
	PermissionPlayer getPlayer(@Nonnull CloudOfflinePlayer player);


}
