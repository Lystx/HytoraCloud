package de.lystx.hytoracloud.driver.player.permission.impl;

import de.lystx.hytoracloud.driver.utils.interfaces.Identifiable;
import de.lystx.hytoracloud.driver.connection.protocol.requests.base.DriverQuery;

import java.util.List;

public interface IPermissionUser extends Identifiable {

    /**
     * Checks if the {@link IPermissionUser} has a certain Permission
     *
     * @param permission the permission to check
     * @return true if has permission
     */
    boolean hasPermission(String permission);

    /**
     * Gets the cached {@link PermissionGroup} from the PermissionPool
     * might be null if not cached yet!
     *
     * @return permissionGroup or null
     */
    PermissionGroup getCachedPermissionGroup();

    /**
     * Gets the {@link PermissionGroup} directly from the Cloud itsself
     * This might take some time to process but it will 100% be sync with the cloud
     *
     * @return response containing permissionGroup
     */
    DriverQuery<PermissionGroup> getPermissionGroup();

    /**
     * Adds a permission to the user
     *
     * @param permission the permission
     */
    DriverQuery<Boolean> addPermission(String permission);

    /**
     * Removes a permission from the user
     *
     * @param permission the permission
     */
    DriverQuery<Boolean> removePermission(String permission);

    /**
     * Lists all permissions the user has
     * (Groups, private and so on)
     *
     * @return list of strings
     */
    List<String> getPermissions();

    /**
     * Lists all the permissions only this user has
     *
     * @return list of strings
     */
    List<String> getExclusivePermissions();

    /**
     * Lists all active (not expired) {@link PermissionGroup}s
     *
     * @return list of groups
     */
    List<PermissionGroup> getAllPermissionGroups();

    /**
     * Gets a {@link PermissionGroup} by name
     *
     * @param name the name of the group
     * @return permissionGroup or null (if not found)
     */
    default PermissionGroup getPermissionGroupByName(String name) {
        return this.getAllPermissionGroups().stream().filter(permissionGroup -> permissionGroup.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    /**
     * Removes a {@link PermissionGroup} from the user
     *
     * @param permissionGroup the group to be removed
     */
    DriverQuery<PermissionGroup> removePermissionGroup(PermissionGroup permissionGroup);

    /**
     * Adds a {@link PermissionGroup} to the user for a given timeUnit
     *
     * @param permissionGroup the group to add
     * @param time the time (e.g. "1")
     * @param unit the unit (e.g. "month")
     */
    DriverQuery<PermissionGroup> addPermissionGroup(PermissionGroup permissionGroup, int time, PermissionValidity unit);

    /**
     * Updates and performs all changes
     */
    void update();

    /**
     * Adds a {@link PermissionGroup} to the user lifetime
     *
     * @param permissionGroup the group to be added
     */
    default DriverQuery<PermissionGroup> addPermissionGroup(PermissionGroup permissionGroup) {
        return this.addPermissionGroup(permissionGroup, -1, PermissionValidity.LIFETIME);
    }
}
