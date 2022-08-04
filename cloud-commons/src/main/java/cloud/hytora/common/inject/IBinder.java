package cloud.hytora.common.inject;

public interface IBinder<T> {

    /**
     * Sets the name of the "annotated with" feature
     *
     * @param name the name
     * @return the current binder
     */
    IBinder<T> annotatedWith(String name);

    /**
     * Binds the class to a given instance
     *
     * @param instance the instance object
     */
    void toInstance(T instance);
}
