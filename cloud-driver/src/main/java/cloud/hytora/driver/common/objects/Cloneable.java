package cloud.hytora.driver.common.objects;

/**
 * Objects inheriting this interface are internally cloneable
 * That means that the internal fields of an object can be cloned
 * from the provided object in {@link Cloneable#clone(Object)}
 * and should be override.
 *
 *
 * @param <T> the generic object type that should be cloned
 */
public interface Cloneable<T> {

    /**
     * Clones this object instance and sets the
     * fields of this object to those of the provided object
     *
     * @param from the provided object to be cloned from
     */
    void clone(T from);
}
