
package cloud.hytora.simplejson.elements.object;

import cloud.hytora.simplejson.api.SimpleProvider;
import cloud.hytora.simplejson.api.modules.ParserModule;
import cloud.hytora.simplejson.api.modules.SerializerModule;
import cloud.hytora.simplejson.elements.JsonEntity;
import cloud.hytora.simplejson.elements.JsonLiteral;
import cloud.hytora.simplejson.api.enums.JsonFormat;
import cloud.hytora.simplejson.api.enums.JsonType;
import cloud.hytora.simplejson.exceptions.JsonSerializerNotFoundException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class JsonObject extends JsonEntity implements Iterable<JsonEntry> {

    /**
     * The keys
     */
    private List<String> names;

    /**
     * The values for all the keys
     */
    private List<JsonEntity> values;

    /**
     * The hash table for storing values
     */
    private HashIndexTable table;

    /**
     * Constructs an empty object
     */
    public JsonObject() {
        if (SimpleProvider.getInstance().getSerializerModule() == null) {
            throw new JsonSerializerNotFoundException("Please instantiate a new Json instance using JsonBuilder");
        }
        this.names = new ArrayList<>();
        this.values = new ArrayList<>();
        this.table = new HashIndexTable();

    }

    /**
     * Constructs an empty object
     */
    public JsonObject(JsonFormat format) {
        this();
        this.format = format;
    }

    /**
     * Constructs a {@link JsonObject} from String input
     *
     * @param input the input to parse
     */
    public JsonObject(String input) {
        this();

        ParserModule parserModule = SimpleProvider.getInstance().getParserModule();

        if (parserModule != null) {

            try {
                JsonObject jsonObject = parserModule.parse(input).asJsonObject();

                this.table = new HashIndexTable();
                this.names = new LinkedList<>(jsonObject.names);
                this.values = new LinkedList<>(jsonObject.values);

                for (int i = 0; i < names.size(); i++) {
                    table.add(names.get(i), i);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Creates a new {@link JsonObject} from a given byte-array
     * It will load the object out of the array by reading it
     *
     * @param bytes the data to read
     */
    public JsonObject(byte[] bytes) {
        this();

        ParserModule parserModule = SimpleProvider.getInstance().getParserModule();

        if (parserModule != null) {

            try (InputStreamReader stream = new InputStreamReader(new ByteArrayInputStream(bytes))) {
                JsonEntity element = parserModule.parse(stream);
                if (!element.isJsonObject()) {
                    this.addAll(new JsonObject());
                    return;
                }

                this.addAll(element.asJsonObject());
            } catch (final Throwable throwable) {
                throwable.printStackTrace();
                this.addAll(new JsonObject());
            }
        }
    }

    /**
     * Creates a new {@link JsonObject} from a given {@link File}
     * It will load the object out of the file by reading it
     *
     * @param file the file
     */
    public JsonObject(File file) {
        this();

        ParserModule parserModule = SimpleProvider.getInstance().getParserModule();

        if (parserModule != null) {

            try {
                JsonObject jsonObject = new JsonObject();
                if (file.exists()) {
                    jsonObject = parserModule.parse(new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))).asJsonObject();
                }

                this.table = new HashIndexTable();
                this.names = new LinkedList<>(jsonObject.names);
                this.values = new LinkedList<>(jsonObject.values);

                for (int i = 0; i < names.size(); i++) {
                    table.add(names.get(i), i);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Fills this {@link JsonObject} with an Object
     * It will be serialized to a {@link JsonObject}
     * and all its values will be set in here
     *
     * @param value the object to fill this json object with
     * @return created object
     */
    public JsonObject addAll(JsonObject value) {

        this.names = value.names;
        this.values = value.values;
        this.table = value.table;
        for (int i = 0; i < names.size(); i++) {
            table.add(names.get(i), i);
        }

        return this;
    }

    /**
     * Adds a {@link String} to this {@link JsonObject}
     *
     * @param key   the key
     * @param value the value to store
     * @return current object
     */
    public JsonObject addProperty(String key, String value) {
        return this.addProperty(key, JsonEntity.valueOf(value));
    }

    /**
     * Adds a {@link Number} to this {@link JsonObject}
     *
     * @param key   the key
     * @param value the value to store
     * @return current object
     */
    public JsonObject addProperty(String key, Number value) {
        return this.addProperty(key, JsonEntity.valueOf(value));
    }

    /**
     * Adds a {@link Boolean} to this {@link JsonObject}
     *
     * @param key   the key
     * @param value the value to store
     * @return current object
     */
    public JsonObject addProperty(String key, boolean value) {
        return this.addProperty(key, JsonEntity.valueOf(value));
    }


    /**
     * Adds a {@link JsonEntity} to this entity
     * (It will be appended under a given key)
     *
     * @param key   the key where to store
     * @param value the value to store
     * @return current object
     */
    public JsonObject addProperty(String key, JsonEntity value) {
        table.add(key, names.size());
        names.add(key);
        values.add(value);

        return this;
    }

    /**
     * Sets a {@link JsonEntry} at a given name in this entity
     * If a value already exists it will simply override the value
     *
     * @param key   the key where to store
     * @param value the value to store
     * @return current object
     */
    public JsonObject setProperty(String key, JsonEntity value) {
        int index = indexOf(key);
        if (index != -1) {
            values.set(index, value);
        } else {
            table.add(key, names.size());
            names.add(key);
            values.add(value);
        }
        return this;
    }

    /**
     * Sets a {@link String} to this {@link JsonObject}
     * If a value already exists it will simply override the value
     *
     * @param key   the key
     * @param value the value to store
     * @return current object
     */
    public JsonObject setProperty(String key, String value) {
        return this.setProperty(key, JsonEntity.valueOf(value));
    }

    /**
     * Sets a {@link Number} to this {@link JsonObject}
     * If a value already exists it will simply override the value
     *
     * @param key   the key
     * @param value the value to store
     * @return current object
     */
    public JsonObject setProperty(String key, Number value) {
        return this.setProperty(key, JsonEntity.valueOf(value));
    }

    /**
     * Sets a {@link Boolean} to this {@link JsonObject}
     * If a value already exists it will simply override the value
     *
     * @param key   the key
     * @param value the value to store
     * @return current object
     */
    public JsonObject setProperty(String key, boolean value) {
        return this.setProperty(key, JsonEntity.valueOf(value));
    }

    /**
     * Removes a stored value of this object under a given key
     *
     * @param key the key where its stored
     */
    public void remove(String key) {
        int index = this.indexOf(key);

        //It contains the key
        if (index != -1) {
            table.remove(index);
            names.remove(index);
            values.remove(index);
        }
    }

    /**
     * Gets a raw {@link JsonEntity} under a stored key
     *
     * @param key the key
     * @return entity or null if not containing
     */
    public JsonEntity get(String key) {
        int index = this.indexOf(key);
        return index != -1 ? values.get(index) : (SimpleProvider.getInstance().getSerializerModule().isProvideNulledObjectsAsRealNull() ? null : JsonLiteral.NULL);
    }

    /**
     * Gets the index of an object stored under a given key
     *
     * @param key the key of the object stored
     * @return index of object
     */
    public int indexOf(String key) {
        int index = table.get(key);
        if (index != -1 && key.equals(names.get(index))) {
            return index;
        }
        return names.lastIndexOf(key);
    }

    /**
     * Checks if this object contains a key
     *
     * @param key the key
     * @return boolean
     */
    public boolean has(String key) {
        return this.get(key) != null;
    }

    /**
     * Clears this object
     * and simply removes all keys
     */
    public void clear() {
        this.keySet().forEach(this::remove);
    }

    /**
     * Gets a {@link Set} containing all the keys of this object
     */
    public Set<String> keySet() {
        Set<String> keys = new HashSet<>();
        for (int i = 0; i < this.size(); i++) {
            keys.add(this.names.get(i));
        }
        return keys;
    }

    /**
     * Gets the size of this object
     */
    public int size() {
        return names.size();
    }

    /**
     * Checks if this object is empty
     */
    public boolean isEmpty() {
        return names.isEmpty() && values.isEmpty();
    }

    public byte[] getBytes() {
        return this.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public Iterator<JsonEntry> iterator() {
        Iterator<String> names = this.names.iterator();
        Iterator<JsonEntity> values = this.values.iterator();
        return new Iterator<JsonEntry>() {

            public JsonEntry next() {
                JsonEntity value = values.next();
                String name = names.next();
                return new JsonEntry(name, value);
            }

            public boolean hasNext() {
                return names.hasNext();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public JsonType jsonType() {
        return JsonType.OBJECT;
    }

    @Override
    public boolean isJsonObject() {
        return true;
    }

    @Override
    public JsonObject asJsonObject() {
        return this;
    }

    /**
     * Saves this {@link JsonObject} to a specific {@link File}
     * with a given {@link JsonFormat}
     *
     * @param file the file to save it to
     */
    public void save(File file) {
        this.save(file, this.format);
    }
    /**
     * Saves this {@link JsonObject} to a specific {@link File}
     * with a given {@link JsonFormat}
     *
     * @param file the file to save it to
     */
    public void save(File file, JsonFormat format) {
        try {
            if (!file.exists() && file.createNewFile()) {
                this.save(file);
                return;
            }
            PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8), true);
            w.print(this.toString(format));
            w.flush();
            w.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class Builder {

        /**
         * The json instance
         */
        private final SerializerModule serializerModule;

        /**
         * The container
         */
        private final JsonObject container;

        /**
         * Constructs a new Builder with a pre defined {@link SerializerModule} instance
         * to be able to serialize every input object
         *
         * @param serializer the instance for serialization
         */
        public Builder(SerializerModule serializer) {
            this.serializerModule = serializer;

            this.container = new JsonObject();
        }

        /**
         * Deprecated because it requires that a {@link SerializerModule}
         * instance has been built before to use this constructor
         * It's safer to just provide your instance in the constructor above
         */
        @Deprecated
        public Builder() {
            this(SimpleProvider.getInstance().getSerializerModule());
        }

        /**
         * Appends a value to the container {@link JsonObject} of this builder
         *
         * @param key   the key where to store the value under
         * @param value the value to store
         * @return current builder instance
         */
        public Builder append(String key, Object value) {
            this.container.addProperty(key, this.serializerModule.toJson(value));
            return this;
        }

        /**
         * "Builds" the object meaning it just
         * returns the object that was cached
         */
        public JsonObject build() {
            return this.container;
        }
    }

}
