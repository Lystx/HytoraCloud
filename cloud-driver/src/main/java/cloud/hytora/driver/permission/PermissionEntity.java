package cloud.hytora.driver.permission;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;

import java.util.Collection;

/**
 * Objects implementing this Interface indicate that they are able
 * to hold permissions and are able to give information about what
 * permissions they have and which not.
 * <br><br>
 * You can also add/remove permissions from this entity
 *
 * @author Lystx
 * @since SNAPSHOT-1.3
 */
public interface PermissionEntity extends IBufferObject {

    /**
     * Adds a provided permission to this entity
     * The permission is permanent when being added
     *
     * @param permission the permission instance
     * @see Permission to see how to construct
     */
    void addPermission(Permission permission);

    /**
     * Removes a given permission from this entity
     * The expirationDate is being ignored when removing
     *
     * @param permission the permission instance
     * @see Permission to see how to construct
     */
    void removePermission(Permission permission);

    /**
     * All cached {@link Permission} instances of this entity
     */
    Collection<Permission> getPermissions();

    /**
     * Searches for a {@link Permission} instance within the permissions
     * this entity owns through calling {@link #getPermissions()} and looks for
     * an instance that matches the permission name with the provided one<br><br>
     *
     * @param permission the name of the permission
     * @return task holding the permission
     * @see #getPermissionOrNull(String)
     */
    Task<Permission> getPermission(String permission);

    /**
     * Searches for a {@link Permission} instance within the permissions
     * this entity owns through calling {@link #getPermissions()} and looks for
     * an instance that matches the permission name with the provided one<br><br>
     *
     * @param permission the name of the permission
     * @return the permission instance or null if none matching
     * @see #getPermissionOrNull(String)
     */
    Permission getPermissionOrNull(String permission);

    /**
     * Method to check if this entity has a specific permission
     * that is also not expired<br>
     *
     * <b>ATTENTION:</b> if this entity owns the "*" permission this method always returns true<br><br>
     *
     *
     * @param permission the name of the permission to check for
     * @return boolean if owns permission
     */
    boolean hasPermission(String permission);

    /**
     * Method to check if this entity has a specific permission
     * that is also not expired<br>
     *
     * <b>ATTENTION:</b> if this entity owns the "*" permission this method always returns true<br><br>
     *
     *
     * @param permission the permission instance to check
     * @return boolean if owns permission
     */
    boolean hasPermission(Permission permission);

    /**
     * Updates this entity and syncs its data all over the network
     */
    void update();
}
