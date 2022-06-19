package cloud.hytora.driver.permission;

import cloud.hytora.driver.player.CloudOfflinePlayer;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;


// TODO: 02.03.2022 documentation
public interface PermissionManager {

	Permission createPermission(String permission, long expirationDate);

	@Nonnull
	@CheckReturnValue
	default PermissionGroup createGroup(@Nonnull String name, int sortId) {
		return createGroup(name, "", "", "", "", sortId);
	}

	@Nonnull
	@CheckReturnValue
	default PermissionGroup createGroup(@Nonnull String name, @Nonnull String color, @Nonnull String chatColor, @Nonnull String tabPrefix, @Nonnull String namePrefix, int sortId) {
		return createGroup(name, color, chatColor, tabPrefix, namePrefix, sortId, false, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
	}

	@Nonnull
	@CheckReturnValue
	PermissionGroup createGroup(@Nonnull String name, @Nonnull String color, @Nonnull String chatColor, @Nonnull String tabPrefix, @Nonnull String namePrefix, int sortId, boolean defaultGroup,
	                            @Nonnull Collection<String> groups, @Nonnull Collection<String> permissions, @Nonnull Collection<String> deniedPermissions);

	@Nonnull
	Collection<PermissionGroup> getAllCachedPermissionGroups();

	@Nullable
	PermissionGroup getGroupByName(@Nonnull String name);

	void removeGroup(@Nonnull String name);

	void removeGroup(@Nonnull PermissionGroup group);

	void saveGroup(@Nonnull PermissionGroup group);

	@Nonnull
	PermissionPlayer getPlayer(@Nonnull CloudOfflinePlayer player);

	@Nullable
	PermissionPlayer getPlayerByUniqueId(@Nonnull UUID uniqueId);

	@Nullable
	PermissionPlayer getPlayerByName(@Nonnull String name);

}
