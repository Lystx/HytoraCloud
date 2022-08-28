package cloud.hytora.driver.services.utils;

/**
 * Different visibilities that can be used in the api (e.g. sign-selector) to check which servers
 * should be visible to other players
 *
 * @author Lystx
 * @since SNAPSHOT-1.2
 */
public enum ServiceVisibility {

    /**
     * This is the default visibility
     * (it has not been changed yet)
     */
    NONE,

    /**
     * The server is visible to others
     */
    VISIBLE,

    /**
     * The server is not visible to others
     */
    INVISIBLE;

    /**
     * Returns a formatted String depending on the state
     */
    public String toString() {
        return this == NONE ? "§7Unknown" : (this == VISIBLE ? "§aVisible" : "§cInvisible");
    }
}
