
package cloud.hytora.driver.provider;

import cloud.hytora.common.task.Task;

import java.util.Collection;

public interface ProviderRegistry {

    /**
     * Sets the instance of a new provider in the cache
     * If it is already cached nothing will happen unless the provider is not immutable
     * See below for explanation
     *
     * @param service the class of the service (interface) you want to set the wrapper for
     * @param provider the provider instance you want to set
     * @param immutable if its immutable (not modifiable)
     * @param needsReplacement if it needs a replacement
     * @param <T> the generic of the provider (of any type)
     * @throws ProviderImmutableException if a provider already exists and is immutable (not modifiable)
     */
    <T> Task<T> setProvider(Class<T> service, T provider, boolean immutable, boolean needsReplacement) throws ProviderImmutableException;

    /**
     * Helper-Method for {@link ProviderRegistry#setProvider(Class, Object, boolean, boolean)} but it sets immutable to false
     *
     * @param service the class of the service (interface) you want to set the wrapper for
     * @param provider the provider instance you want to set
     * @param <T> the generic of the provider (of any type)
     * @throws ProviderImmutableException if a provider already exists and is immutable (not modifiable)
     */
    default <T> Task<T> setProvider(Class<T> service, T provider) throws ProviderImmutableException {
        return this.setProvider(service, provider, false);
    }

    /**
     * Helper-Method for {@link ProviderRegistry#setProvider(Class, Object, boolean, boolean)} but it sets needsReplacement to false
     *
     * @param service the class of the service (interface) you want to set the wrapper for
     * @param provider the provider instance you want to set
     * @param immutable if its immutable (not modifiable)
     * @param <T> the generic of the provider (of any type)
     * @throws ProviderImmutableException if a provider already exists and is immutable (not modifiable)
     */
    default <T> Task<T> setProvider(Class<T> service, T provider, boolean immutable) throws ProviderImmutableException {
        return this.setProvider(service, provider, immutable, false);
    }

    /**
     * Gets an {@link Task} instance of a provider
     * This might contain nothing but at least it does not throw exceptions
     * if there is no specified provider for the provided class
     *
     * @param service the class of the provider you are trying to get
     * @param <T> the generic of the provider (of any type)
     * @return optional instance containing provider or not
     */
    <T> Task<T> get(Class<T> service);

    /**
     * Gets a provider instance without any checks
     * Unlike {@link ProviderRegistry#get(Class)} this method
     * does throw an exception if there is no such registered provider for the provided class type
     *
     * @param service the class of the provider you are trying to get
     * @param <T> the generic of the provider (of any type)
     * @return the found instance
     * @throws ProviderNotRegisteredException if there is no such provider
     */
    <T> T getProvider(Class<T> service) throws ProviderNotRegisteredException;

    /**
     * All registered {@link ProviderEntry}s of this registry
     *
     * @see ProviderEntry
     */
    Collection<ProviderEntry<?>> getEntries();

    /**
     * Gets an {@link Task} instance of an entry
     * This might contain nothing but at least it does not throw exceptions
     * if there is no specified provider for the provided class
     *
     * @param service the class of the provider you are trying to get
     * @param <T> the generic of the provider (of any type)
     * @return optional instance containing entry or not
     *
     * @see ProviderEntry
     */
    <T> Task<ProviderEntry<T>> getEntry(Class<T> service);

    /**
     * Gets an entry instance without any checks
     * Unlike {@link ProviderRegistry#getEntry(Class)} this method
     * does throw an exception if there is no such registered provider for the provided class type
     *
     * @param service the class of the provider you are trying to get
     * @param <T> the generic of the provider (of any type)
     * @return the found instance
     * @throws ProviderNotRegisteredException if there is no such provider
     */
    <T> ProviderEntry<T> getEntryUnchecked(Class<T> service) throws ProviderNotRegisteredException;

    /**
     * Unregisters a provider from cache if its cached
     * But this method can also replace an already existing provider if you provide the parameter "replacement"
     * If it's not cached yet, it will throw exceptions but see below!
     *
     * @param service the class of the provider you are trying to unregister
     * @param replacement if you want to unregister the provider and replace it with a new one
     * @param <T> the generic of the provider (of any type)
     * @throws ProviderNotRegisteredException if there is no such provider
     * @throws ProviderImmutableException if the provider may not be modified
     * @throws ProviderNeedsReplacementException if the provider needs a replacement and the replacement is null
     */
    <T> void unregister(Class<T> service, T replacement) throws ProviderNotRegisteredException, ProviderImmutableException, ProviderNeedsReplacementException;

    /**
     * Helper-Method for {@link ProviderRegistry#unregister(Class, T)} but it intends
     * that you don't want to replace the service but that you are trying to unregister it without any replacement
     *
     * @param service the class of the provider you are trying to unregister
     * @param <T> the generic of the provider (of any type)
     * @throws ProviderNotRegisteredException if there is no such provider
     * @throws ProviderImmutableException if the provider may not be modified
     * @throws ProviderNeedsReplacementException if the provider needs a replacement and the replacement is n
     */
    default <T> void unregister(Class<T> service) throws ProviderNotRegisteredException, ProviderImmutableException, ProviderNeedsReplacementException {
        this.unregister(service, null);
    }

    /**
     * Checks if a certain provider of a class is registered
     *
     * @param service the class of the provider you are trying to check
     * @return true if it's registered | false if it's not registered
     */
    boolean isRegistered(Class<?> service);
}
