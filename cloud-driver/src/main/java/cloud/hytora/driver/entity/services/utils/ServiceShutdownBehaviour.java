package cloud.hytora.driver.entity.services.utils;

/**
 * The {@link ServiceShutdownBehaviour} defines what happens with a {@link cloud.hytora.driver.entity.services.CloudService}s
 * {@link java.io.File}s after shutdown of the server
 *
 *
 * @author Lystx
 * @since DEV-0.2
 * @version DEV-0.4
 */
public enum ServiceShutdownBehaviour {

    /**
     * All files of the server will be kept
     * (also known as 'static')
     */
    KEEP,

    /**
     * All files of the server will be deleted
     * (also known as 'dynamic')
     */
    DELETE;

    public boolean isStatic() {
        return this == KEEP;
    }

    public boolean isDynamic() {
        return this == DELETE;
    }
}
