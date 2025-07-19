package cloud.hytora.common.identification;

/**
 * Objects that inherit this class have a Name set to them
 * This name is immutable and can not be changed.
 *
 * @author Lystx
 * @since DEV-1.0
 * @version DEV-1.1
 */
public interface ImmutableNameHolder {

    /**
     * @return the final name of this object
     */
    String getName();
}
