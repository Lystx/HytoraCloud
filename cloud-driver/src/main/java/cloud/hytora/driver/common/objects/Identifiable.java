package cloud.hytora.driver.common.objects;

/**
 * Objects that inherit this interface
 * have a main identity.
 * e.g. (players => their name, server: the name of the server)
 */
public interface Identifiable {

    /**
     * What is always preferred to be the identity
     */
    String getMainIdentity();
}
