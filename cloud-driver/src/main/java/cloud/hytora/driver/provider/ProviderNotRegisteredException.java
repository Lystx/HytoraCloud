
package cloud.hytora.driver.provider;


public class ProviderNotRegisteredException extends RuntimeException {

    private static final long serialVersionUID = -4706963432668698438L;

    public ProviderNotRegisteredException(Class<?> service) {
        super("No provider for service " + service.getName() + " is registered");
    }
}
