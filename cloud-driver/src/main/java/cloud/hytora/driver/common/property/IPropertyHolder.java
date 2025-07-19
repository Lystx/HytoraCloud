package cloud.hytora.driver.common.property;

import cloud.hytora.document.Document;
import cloud.hytora.document.IEntry;
import cloud.hytora.document.JsonEntity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * Objects implementing this interface are able to hold properties.
 * The properties of {@link IPropertyHolder}s are managed internally in form of Json ({@link Document})
 *
 *
 * @see cloud.hytora.driver.entity.player.CloudPlayer
 * @see cloud.hytora.driver.entity.services.task.ServiceTask
 * @author Lystx
 * @version SNAPSHOT-1.5
 */
public interface IPropertyHolder {

    /**
     * The properties of this holder
     * Properties are never null, but they can be empty.
     *
     * @return document instance.
     */
    @NotNull
    Document getProperties();


    /**
     * Sets the whole property object of this holder
     * This method does not update the properties over the network
     * You need to individually update the instance of this holder
     *
     * @param properties the properties to set
     */
    void setProperties(@NotNull Document properties);

    /**
     * Tries to retrieve the given property in form of {@link IEntry}
     * Not set properties can result in nulled values!
     *
     * @param name the key of the property
     * @return the found instance or null
     */
    @Nullable
    IEntry getProperty(String name);

    /**
     * Checks if this holder has the given property
     *
     * @param name the key of the property
     * @return boolean value
     */
    boolean hasProperty(String name);

    /**
     * Sets a specific property of this holder to a given value
     * This method does not update the properties over the network
     * You need to individually update the instance of this holder
     *
     * @param key the key of the property
     * @param value the value to set
     */
    void setProperty(@NotNull String key, @Nullable Object value);

}
