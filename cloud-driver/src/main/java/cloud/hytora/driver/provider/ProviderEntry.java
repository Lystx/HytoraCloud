
package cloud.hytora.driver.provider;

/**
 * Represents an entry you insert when using {@link ProviderRegistry#setProvider(Class, Object, boolean, boolean)}
 * To get information about the created providers you created
 *
 * @param <T> the generic type of the provider of this entry
 */
public interface ProviderEntry<T> {

    /**
     * The class of the provider of this entry
     */
    Class<T> getService();

    /**
     * The provider instance of this entry
     */
    T getProvider();

    /**
     * If this entry may be modified
     */
    boolean isImmutable();

    /**
     * If this entry needs a replacement at some point
     */
    boolean needsReplacement();
}
