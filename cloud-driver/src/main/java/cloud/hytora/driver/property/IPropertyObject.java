package cloud.hytora.driver.property;

import cloud.hytora.document.Document;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * Objects that implement {@link IPropertyObject} hold properties in form of a {@link Document}
 * And you can access this {@link Document} either raw or through the helper methods
 * such as {@link #getProperty(String, Class)} or {@link #setProperty(String, Object)}
 *
 * @author Lystx
 * @since SNAPSHOT-1.2
 */
public interface IPropertyObject {

    /**
     * The raw properties of this object
     * @see Document
     */
    @NotNull
    Document getProperties();

    /**
     * Sets the raw properties of this object
     *
     * @param properties the properties to set
     * @see Document
     */
    void setProperties(@NotNull Document properties);

    /**
     * Returns the value of a property by its key
     *
     * @param key the key of the property
     * @param typeClass the class of the object you're trying to get
     * @param <T> the generic of the object you are trying to get
     * @return instance or null if no property found or wrong type
     */
    @Nullable
    <T> T getProperty(@NotNull String key, @NotNull Class<T> typeClass);

    /**
     * Sets a single property for this object's properties
     *
     * @param key the key for the property
     * @param value the value for the property
     */
    void setProperty(@NotNull String key, @Nullable Object value);

}
