package cloud.hytora.driver.permission;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;

import java.util.concurrent.TimeUnit;

/**
 * This interface describes a Permission that is being added
 * to a certain {@link PermissionEntity}
 * <br><br>
 * This permission consists of two values:<br>
 *      - the name of permission {@link #getPermission()}<br>
 *      - the time in millis when this permission is removed {@link #getExpirationDate()} (-1 = permanent)<br>
 *<br>
 * @author Lystx
 * @since SNAPSHOT-1.3
 */
public interface Permission extends IBufferObject {

    /**
     * Static method to construct a new {@link Permission} instance
     *
     * @param permission the name of the permission
     * @param expirationDate the date of the permission to construct
     * @return new instance or null if {@link PermissionManager} is not defined
     */
    static Permission of(String permission, long expirationDate) {
        return CloudDriver.getInstance().get(PermissionManager.class).mapOrElse(permissionManager -> permissionManager.createPermission(permission, expirationDate), () -> null);
    }

    /**
     * Static method to construct a new {@link Permission} instance
     *
     * @param permission the name of the permission
     * @param expirationUnit the unit of the expiration
     * @param expirationValue the value for the unit
     * @return new instance or null if {@link PermissionManager} is not defined
     */
    static Permission of(String permission, TimeUnit expirationUnit, long expirationValue) {
        long calculatedTimeOut = expirationUnit.toMillis(expirationValue);
        calculatedTimeOut+= System.currentTimeMillis(); //adding current time to timeOut
        return of(permission, calculatedTimeOut);
    }

    /**
     * Static method to construct a permanent {@link Permission} instance
     * that never expires because the expirationDate is set to -1
     *
     * @param permission the name of the permission
     * @return new instance or null if {@link PermissionManager} is not defined
     */
    static Permission of(String permission) {
        return of(permission, -1);
    }

    /**
     * The name of this permission instance
     * (e.g. "system.cloud.test.permission")
     */
    String getPermission();

    /**
     * The time in millis when this permission expires
     * (-1 = permanent)
     */
    long getExpirationDate();

    /**
     * Checks if this permission has expired
     */
    boolean hasExpired();
}
