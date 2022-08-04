package cloud.hytora.common.inject;

public interface Injector {

    /**
     * Binds a class to something
     *
     * @param cls the class
     * @param <T> the generic
     * @return binder object
     */
    <T> IBinder<T> bind(Class<T> cls);

    /**
     * Gets an instance for a given class if registered
     * Is it's not registered an {@link IllegalStateException} will be thrown
     *
     * @param cls the class to get the instance of
     * @param <T> the generic
     * @return object or null
     */
    <T> T getInstance(Class<T> cls);
}
