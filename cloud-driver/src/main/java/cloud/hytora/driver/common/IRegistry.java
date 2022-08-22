package cloud.hytora.driver.common;

import java.util.HashMap;

public interface IRegistry<K, V> {

    /**
     * Gets the value by given {@code key}
     *
     * @param key The key of the registered values
     * @return The object
     */
    V get(K key);

    /**
     * Similar to {@link HashMap#put(Object, Object)} but another name
     *
     * @param key    The key
     * @param object The object to be stored behind the key
     * @return The result
     */
    boolean register(K key, V object);

    boolean register(V... objects);

    /**
     * Similar to {@link HashMap#remove(Object, Object)} but another name
     *
     * @param key   The key
     * @param value The value that was stored behind thee key (to-delete)
     * @return The result
     */
    boolean unregister(K key, V value);


    boolean unregister(V... objects);

}
