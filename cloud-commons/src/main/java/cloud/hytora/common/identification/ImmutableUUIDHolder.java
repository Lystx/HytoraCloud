package cloud.hytora.common.identification;

import java.util.UUID;

/**
 * Objects that inherit this class have a UniqueId set to them
 * This uniqueId is immutable and can not be changed.
 *
 * @author Lystx
 * @since DEV-1.0
 * @version DEV-1.1
 * @see UUID
 */
public interface ImmutableUUIDHolder {

    /**
     * @return the final uuid of this object
     */
    UUID getUniqueId();
}
