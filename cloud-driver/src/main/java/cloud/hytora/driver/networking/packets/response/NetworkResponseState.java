package cloud.hytora.driver.networking.packets.response;

/**
 * The {@link NetworkResponseState} defines the state a query is.
 * It declares if a query was ok or has failed.
 *
 * @author Lystx
 * @since DEV-1.0
 * @version DEV-1.1
 */
public enum NetworkResponseState {

    /**
     * Everything was ok, no errors
     */
    OK,

    /**
     * An error occured
     */
    ERROR,

    /**
     * Bad request internally
     */
    BAD_REQUEST,

    /**
     * Something failed
     */
    FAILED,
}
