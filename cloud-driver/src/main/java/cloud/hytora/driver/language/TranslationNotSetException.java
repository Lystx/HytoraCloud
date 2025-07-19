package cloud.hytora.driver.language;

import cloud.hytora.driver.common.exception.HytoraCloudException;

public class TranslationNotSetException extends HytoraCloudException {

    public TranslationNotSetException() {
    }

    public TranslationNotSetException(String message) {
        super(message);
    }

    public TranslationNotSetException(String message, Throwable cause) {
        super(message, cause);
    }

    public TranslationNotSetException(Throwable cause) {
        super(cause);
    }
}
