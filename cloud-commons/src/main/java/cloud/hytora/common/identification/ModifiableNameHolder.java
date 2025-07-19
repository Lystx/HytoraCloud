package cloud.hytora.common.identification;

/**
 * Objects implementing this interface have a name attached
 * to this object that can also be changed.
 *
 *
 * @author Lystx
 * @version DEV-0.1
 * @since DEV-0.1
 * @see ImmutableNameHolder
 */
public interface ModifiableNameHolder extends ImmutableNameHolder {

    /**
     * Sets and changes the name of this object
     *
     * @param name the name of this object
     */
    void setName(String name);
}
