
package cloud.hytora.driver.provider;


public class ProviderNeedsReplacementException extends RuntimeException {

    private static final long serialVersionUID = -775167073540473896L;

    public ProviderNeedsReplacementException(Class<?> service) {
        super("The service " + service.getName() + " needs a replacement");
    }
}
