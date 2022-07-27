package cloud.hytora.driver.player;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

public interface TemporaryProperties {

    /**
     * Adds a temporary property that times out after a specified time
     *
     * @param name the path of the property
     * @param value the value to save under that property
     * @param time the time value (e.g  1)
     * @param unit the unit for the given time (e.g. SECONDS)
     */
    void addProperty(String name, Object value, long time, TimeUnit unit);

    /**
     * returns a saved property under the given name
     *
     * @param name the name of the property
     * @param typeClass the type class of the object this property is
     * @return instantiated property object
     * @param <T> the generic type of the object
     */
    <T> T getProperty(String name, Class<T> typeClass);


    /**
     * Removes a saved temporary property
     *
     * @param name the name of the property
     */
    void removeProperty(String name);


    /**
     * All cached property names
     */
    Collection<String> getPropertyNames();

    /**
     * Checks if a specific property of the provided name has expired
     *
     * @param name the name of the property
     * @return boolean
     */
    boolean hasPropertyExpired(String name);

    /**
     * Checks if this property contains a property
     *
     * @param name the name of the property to check
     * @return boolean
     */
    boolean hasProperty(String name);
}
