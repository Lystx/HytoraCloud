
package cloud.hytora.simplejson.impl.config;


import cloud.hytora.simplejson.api.config.JsonConfig;
import cloud.hytora.simplejson.api.config.JsonSection;
import cloud.hytora.simplejson.elements.object.JsonObject;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The default implementation of a configuration file that
 * is loaded into memory.
 */

@Getter
public class SimpleJsonConfig extends SimpleJsonSection implements JsonConfig {

    /**
     * The mapping of configs cached by the server.
     *
     * Configs should really only have one instance so this
     * cache holds configs indefinitely.
     */
    public static final ConcurrentMap<Path, SimpleJsonConfig> CACHE = new ConcurrentHashMap<>();

    /**
     * The path to the config file
     */
    private final Path path;

    /**
     * Creates a new config from the given path
     *
     * @param path the path to the config file
     */
    public SimpleJsonConfig(Path path) {
        super("", null, null);
        this.path = path;
    }

    @Override
    public File getFile() {
        return this.path.toFile();
    }

    @Override
    public File getDirectory() {
        return this.path.getParent().toFile();
    }

    @Override
    public JsonSection root() {
        return this;
    }

    @Override
    public JsonSection parent() {
        return this;
    }

    @Override @SneakyThrows
    public void load() {
        File file = path.toFile();
        if (!file.exists()) {
            if (file.createNewFile()) {
                load0(file);
            }
        } else {
            load0(file);
        }
    }

    @SneakyThrows
    private void load0(File file) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (FileInputStream stream = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            while (stream.read(buffer, 0, buffer.length) > -1) {
                out.write(buffer, 0, buffer.length);
            }
        }

        String json = out.toString().trim();
        if (json.trim().isEmpty()) {
            json = "{}";
        }
        JsonObject object = new JsonObject(json);
        this.read(object);
    }

    @Override @SneakyThrows
    public void save()  {
        JsonObject object = this.toJson();
        object.save(path.toFile());
        String json = object.toString();

        try (FileOutputStream stream = new FileOutputStream(path.toFile())) {
            stream.write(json.getBytes());
        }
    }

    @Override
    public void clear() {
        getKeys(false).forEach(this::remove);
    }

    @Override
    public void delete() {
        File file = path.toFile();
        if (file.exists()) {
            file.delete();
        }
        this.clear();
        CACHE.remove(path);
    }
}
