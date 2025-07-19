package cloud.hytora.driver.common.exception;

/**
 * Personal Exception for our network
 */
public class HytoraCloudException extends RuntimeException {

    public HytoraCloudException() {
        super();
    }

    public HytoraCloudException(String message) {
        super(message);
    }

    public HytoraCloudException(String message, Throwable cause) {
        super(message, cause);
    }

    public HytoraCloudException(Throwable cause) {
        super(cause);
    }
}
