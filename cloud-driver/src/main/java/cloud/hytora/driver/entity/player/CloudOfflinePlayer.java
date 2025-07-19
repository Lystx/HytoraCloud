package cloud.hytora.driver.entity.player;

import cloud.hytora.driver.common.PublishingType;
import cloud.hytora.driver.common.exception.ModuleNeededException;
import cloud.hytora.driver.common.objects.CloudJsonEntity;
import cloud.hytora.driver.common.objects.Identifiable;
import cloud.hytora.driver.common.exception.PlayerNotOnlineException;
import cloud.hytora.driver.entity.CloudEntity;
import cloud.hytora.driver.module.permission.PermissionPlayer;
import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;
import cloud.hytora.driver.common.property.IPropertyHolder;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * This player object indicates that a player is offline.
 * From here on you can get the most important information even if a player is offline.
 *
 * Remember that getting {@link CloudOfflinePlayer} is not safe to use and will always take a bit of time
 * because all the data needs to be read from the provided Database and only then is cached internally.
 * If you request a high amount of offline players this might lead to severe lags.
 *
 * @author Lystx
 * @since DEV-1.0
 * @version SNAPSHOT-1.5
 */
public interface CloudOfflinePlayer extends IBufferObject, Identifiable, CloudEntity, CloudJsonEntity, IPropertyHolder {

    /**
     * The cached name of this offline player entry
     * (Note that names might have changed between log-ins)
     */
    @NotNull
    String getName();

    /**
     * Override method to check if this player has
     * the requested permission set
     *
     * @param perm the permission to check
     * @return if has permission
     */
    boolean hasPermission(String perm);

    /**
     * Checks if this player is currently online
     */
    boolean isOnline();

    /**
     * Tries to get this player as online player
     */
    CloudPlayer asOnlinePlayer() throws PlayerNotOnlineException;

    /**
     * Tries to get the {@link PermissionPlayer} of this player
     * (Note: this method requires the house-own permissionModule)
     *
     * @throws ModuleNeededException if the perms-module is not loaded
     */
    @Nonnull
    PermissionPlayer asPermissionPlayer() throws ModuleNeededException;

    /**
     * Overrides the name of this cached player entry
     * because the name might have changed over time
     * to keep correct offline player entries
     *
     * @param name the name of this player entry
     */
    void setName(@Nonnull String name);

    /**
     * The cached uuid of this offline player entry
     */
    @NotNull
    UUID getUniqueId();

    /**
     * The time as long (date in millis) when this player has
     * logged in for the first time on this network
     */
    long getFirstLogin();

    /**
     * Sets the first login of this player when joining
     * (Only modify if you know what you're doing)
     *
     * @param time the time to set
     */
    void setFirstLogin(long time);

    /**
     * The time as long (date in millis) when the player
     * lastly joined the network
     */
    long getLastLogin();

    /**
     * Sets the last login of this player when joining
     * (Only modify if you know what you're doing)
     *
     * @param time the time to set
     */
    void setLastLogin(long time);

    /**
     * Saves this player's data into the database
     *
     * and if executed on {@link CloudPlayer} also calls {@link CloudPlayer#update(PublishingType...)}
     *
     * @see CloudPlayer#update(PublishingType...)
     */
    void save();

}
