
package cloud.hytora.simplejson.api.config;

import java.io.File;
import java.nio.file.Path;

/**
 * This class represents the configuration file loaded into
 * memory.
 *
 * <p>Config files may be created using the static load
 * factories. These methods have no exception thrown because
 * exceptions occurring during this time are a sign of a
 * serious underlying problem and thus should not be
 * swallowed.</p>
 */

public interface JsonConfig extends JsonSection {

    /**
     * Obtains the configuration file which stores the data
     * held in the config.
     *
     * @return the config as a file
     */
    File getFile();

    /**
     * Obtains the path object which stores the data held in
     * the config.
     *
     * @return the config as a path
     */
    Path getPath();

    /**
     * Obtains the folder which contains this configuration
     * file.
     *
     * @return the container folder
     */
    File getDirectory();

    /**
     * Loads (or reloads) data from the file into memory.
     */
    void load();

    /**
     * Saves the data in memory to file.
     */
    void save();

    /**
     * Clears the data in memory
     */
    void clear();

    /**
     * Deletes this config (file) and from cache
     */
    void delete();
}
