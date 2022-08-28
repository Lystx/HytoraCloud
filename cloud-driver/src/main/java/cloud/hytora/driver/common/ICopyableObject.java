package cloud.hytora.driver.common;

/**
 * Objects that implement this interface can copy their internal values
 * from another object of their generic type
 *
 * @param <T> the generic object
 * @author Lystx
 * @since SNAPSHOT-1.3
 */
public interface ICopyableObject<T> {

    void copy(T from);
}
