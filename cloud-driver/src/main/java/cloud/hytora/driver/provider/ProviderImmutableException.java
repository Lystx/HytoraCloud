
package cloud.hytora.driver.provider;


public class ProviderImmutableException extends RuntimeException {

    private static final long serialVersionUID = -4558245546379032533L;

    public ProviderImmutableException(Class<?> service) {
        super("The provider of service " + service.getName() + " is immutable");
    }
}
