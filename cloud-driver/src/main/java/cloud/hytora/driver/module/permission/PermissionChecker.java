package cloud.hytora.driver.module.permission;

import cloud.hytora.driver.command.sender.CommandSender;

import java.util.UUID;

/**
 * The {@link PermissionChecker} is used to implement different permission-checks
 * depending on the environment this instance is running on.
 *
 * @version DEV-1.0
 * @since DEV-1.0
 * @author Lystx
 */
public interface PermissionChecker {


    /**
     * Root method to check if a player with the specified {@link UUID}
     * is holding the provided permission in form of a {@link String}
     *
     * @param playerUniqueId the uuid of the player
     * @param permission the permission to check for
     * @return boolean value
     */
    boolean hasPermission(UUID playerUniqueId, String permission);
}
