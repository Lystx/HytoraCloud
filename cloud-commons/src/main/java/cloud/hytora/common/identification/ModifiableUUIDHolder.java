package cloud.hytora.common.identification;

import java.util.UUID;

/**
 * Objects implementing this interface have a uuid attached
 * to this object that can also be changed.
 *
 * @author Lystx
 * @version DEV-0.1
 * @since DEV-0.1
 * @see ImmutableUUIDHolder
 */
public interface ModifiableUUIDHolder extends ImmutableUUIDHolder {

    /**
     * Sets and changes the uniqueId of this object
     *
     * @param uniqueId the uniqueId of this object
     */
    void setUniqueId(UUID uniqueId);
}
