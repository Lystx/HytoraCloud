
package cloud.hytora.simplejson.impl.config;

import cloud.hytora.simplejson.api.SimpleProvider;
import cloud.hytora.simplejson.api.config.JsonSection;
import cloud.hytora.simplejson.elements.JsonArray;
import cloud.hytora.simplejson.elements.JsonEntity;
import cloud.hytora.simplejson.elements.object.JsonEntry;
import cloud.hytora.simplejson.elements.object.JsonObject;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Implementation of a configuration section
 */
@Getter
public class SimpleJsonSection implements JsonSection {

    /**
     * The name of this config section (empty if root)
     */
    private final String name;

    /**
     * The parent config section, or null if root
     */
    private final JsonSection parent;

    /**
     * The root config section, or null if root
     */
    private final JsonSection root;

    /**
     * The elements of the configuration file
     */
    private final BasedMap<Object> elements;

    /**
     * Creates a new config section.
     *
     * @param name   the name of the new config section
     * @param parent the parent of the child section
     * @param root   the root section
     */
    public SimpleJsonSection(String name, JsonSection parent, JsonSection root) {
        this.name = name;
        this.parent = parent;
        this.root = root;

        this.elements = new BasedMap<>();
    }

    @Override
    public JsonSection root() {
        return this.root;
    }

    @Override
    public JsonSection parent() {
        return this.parent;
    }

    @Override
    public JsonSection createChild(String key) {
        String[] split = key.split(Pattern.quote("."));

        SimpleJsonSection section = this;
        if (split.length > 0) {
            for (String aSplit : split) {
                SimpleJsonSection child = section.getChild(aSplit);
                if (child != null) {
                    section = child;
                    continue;
                }
                section = section.createChild0(aSplit);
            }
        }

        return section;
    }

    public JsonObject toJson() {
        JsonObject object = new JsonObject();

        // funky code because we need to make sure all the
        // elements remain in insertion order
        this.elements.forEach((k, v) -> {
            if (v instanceof JsonSection) {
                SimpleJsonSection section = (SimpleJsonSection) v;
                object.addProperty(k, section.toJson());
            } else {

                object.addProperty(k, JsonEntity.valueOf(v) == null ? (SimpleProvider.getInstance().getSerializerModule() == null ? null : SimpleProvider.getInstance().getSerializerModule().toJson(v)) : JsonEntity.valueOf(v));
            }
        });
        return object;
    }

    /**
     * Loads the json from file into memory
     *
     * @param object the file json
     */
    public void read(JsonObject object) {

        for (JsonEntry e : object) {
            String key = e.getName();
            JsonEntity value = e.getValue();

            // special handling for json objects which are
            // config sections
            if (value.isJsonObject()) {
                SimpleJsonSection section = this.createChild0(key);
                section.read(value.asJsonObject());
            } else {

                this.elements.put(key, value.asObject());
            }
        }
    }


    @Override
    public SimpleJsonSection getChild(String key) {
        return this.findSection(key.split(Pattern.quote(".")), false);
    }

    @Override
    public boolean removeChild(String key) {
        String[] split = key.split(Pattern.quote("."));
        String finalKey = split.length > 0 ? split[split.length - 1] : key;

        SimpleJsonSection parent = this.findSection(split, true);
        return parent.elements.remove(finalKey) != null;
    }

    @Override
    public Stream<JsonSection> getChildren(boolean deep) {
        Set<JsonSection> set = new LinkedHashSet<>();
        this.children0(set, deep);
        return Collections.unmodifiableSet(set).stream();
    }

    @Override
    public Stream<String> getKeys(boolean deep) {
        Set<String> set = new LinkedHashSet<>();
        this.iterate("", set, (s, e) -> this.handlePath(s, e.getKey()), deep);
        return Collections.unmodifiableSet(set).stream();
    }

    @Override
    public Stream<Object> getValues(boolean deep) {
        LinkedList<Object> list = new LinkedList<>();
        this.iterate("", list, (s, e) -> e.getValue(), deep);
        return Collections.unmodifiableCollection(list).stream();
    }

    @Override
    public Stream<Map.Entry<String, Object>> getEntries(boolean deep) {
        Set<Map.Entry<String, Object>> set = new LinkedHashSet<>();
        this.iterate("", set, this::concatKey, deep);
        return Collections.unmodifiableSet(set).stream();
    }

    @Override
    public Object get(String key) {
        return this.getElement(key);
    }

    @Override
    public <T> T get(String key, Class<T> type) {
        Object element = this.getElement(key);
        if (!(element instanceof JsonSection)) {
            return (T) element;
        }

        throw new IllegalArgumentException(key + " is a config section");
    }

    @Override
    public void set(String key, Object value) {
        String[] split = key.split(Pattern.quote("."));
        String finalKey = key;

        SimpleJsonSection section = this;
        if (split.length > 0) {
            finalKey = split[split.length - 1];
            for (int i = 0; i < split.length - 1; i++) {
                section = (SimpleJsonSection) section.createChild(split[i]);
            }
        }

        if (value instanceof Collection) {
            JsonArray v = new JsonArray();
            for (Object o : (Collection) value) {
                v.add(JsonEntity.valueOf(o));
            }
            value = v;
        }

        section.elements.put(finalKey, value);
    }

    @Override
    public boolean remove(String key) {
        String[] split = key.split(Pattern.quote("."));
        String finalKey = split.length == 0 ? key : split[split.length - 1];
        SimpleJsonSection section = this.findSection(split, true);
        return section.elements.remove(finalKey) != null;
    }

    @Override
    public boolean hasKey(String key) {
        return this.elements.containsKey(key);
    }

    @Override
    public int getInt(String key) {
        return this.get(key, Number.class).intValue();
    }

    @Override
    public void setInt(String key, int value) {
        this.set(key, value);
    }

    @Override
    public short getShort(String key) {
        return this.get(key, Number.class).shortValue();
    }

    @Override
    public void setShort(String key, short value) {
        this.set(key, value);
    }

    @Override
    public long getLong(String key) {
        return this.get(key, Number.class).longValue();
    }

    @Override
    public void setLong(String key, long value) {
        this.set(key, value);
    }

    @Override
    public byte getByte(String key) {
        return this.get(key, Number.class).byteValue();
    }

    @Override
    public void setByte(String key, byte value) {
        this.set(key, value);
    }

    @Override
    public float getFloat(String key) {
        return this.get(key, Number.class).floatValue();
    }

    @Override
    public void setFloat(String key, float value) {
        this.set(key, value);
    }

    @Override
    public double getDouble(String key) {
        return this.get(key, Number.class).doubleValue();
    }

    @Override
    public void setDouble(String key, double value) {
        this.set(key, value);
    }

    @Override
    public char getChar(String key) {
        return (char) this.getElement(key);
    }

    @Override
    public void setChar(String key, char value) {
        this.set(key, value);
    }

    @Override
    public boolean getBoolean(String key) {
        return (boolean) this.getElement(key);
    }

    @Override
    public void setBoolean(String key, boolean value) {
        this.set(key, value);
    }


    @Override
    public String getString(String key) {
        return (String) this.getElement(key);
    }

    @Override
    public void setString(String key, String value) {
        this.set(key, value);
    }

    @Override
    public <T, C extends Collection<T>> void getCollection(String key, C collection) {
        Object o = this.getElement(key);
        if (o instanceof JsonArray) {
            JsonArray array = (JsonArray) o;
            for (JsonEntity value : array) {
                collection.add((T) value.asObject());
            }
            return;
        }

        throw new NoSuchElementException(String.format("%s is not a collection (%s)", key, o.getClass()));
    }

    // The following two methods are necessary in order to
    // prevent recursing over the same method which creates
    // a collection each time
    // Instead, only the first collection will collect all
    // of the elements instead of creating a new collection
    // each time and copying

    /**
     * Gets all the children config sections.
     *
     * @param col  the collection to append
     * @param deep {@code true} to get the children
     */
    private void children0(Collection<JsonSection> col, boolean deep) {
        this.elements.values().stream()
                .filter(o -> o instanceof JsonSection)
                .map(o -> (SimpleJsonSection) o)
                .forEach(cs -> {
                    if (deep) {
                        cs.children0(col, true);
                    }
                    col.add(cs);
                });
    }

    /**
     * Iterates over the elements in this config section,
     * performing the given operations in order to append
     * the elements to the given collection.
     *
     * @param base     the base string key
     * @param col      the collection to append entries
     * @param function extracts the entry value
     * @param deep     {@code true} to get children elements
     * @param <T>      the type appended to the collection
     */
    private <T> void iterate(String base, Collection<T> col, BiFunction<String, Map.Entry<String, Object>, T> function, boolean deep) {
        this.elements.entrySet().forEach(e -> {
            Object val = e.getValue();
            if (deep) {
                if (val instanceof JsonSection) {
                    SimpleJsonSection section = (SimpleJsonSection) val;
                    section.iterate(this.handlePath(base, section.name), col, function, true);
                    return;
                }
            }

            col.add(function.apply(base, e));
        });
    }

    /**
     * Vera child config section creation method,
     * useful for bypassing the . key based key creation
     * when we know that the name is not . separated.
     *
     * @param name the name of the new child
     * @return the created section
     */
    private SimpleJsonSection createChild0(String name) {
        SimpleJsonSection section = new SimpleJsonSection(name, this, this.root());
        this.elements.put(name, section);
        return section;
    }

    /**
     * Handles whether to append the current path to
     * the base path for relative keys.
     *
     * @param path the base path
     * @param cur  the current path
     * @return the path
     */
    private String handlePath(String path, String cur) {
        if (path.isEmpty()) return cur;
        return path + "." + cur;
    }

    /**
     * Obtains a config section with a key split with .
     *
     * @param split    the split key
     * @param hasValue {@code true} if the key contains a
     *                 value, {@code false} if the key contains only config
     *                 sections
     * @return the config section
     */

    private SimpleJsonSection findSection(String[] split, boolean hasValue) {
        SimpleJsonSection section = this;
        if (split.length > 1) {
            for (int i = 0; i < (hasValue ? split.length - 1 : split.length); i++) {
                String sectionName = split[i];
                Object o = section.elements.get(sectionName);
                if (!(o instanceof JsonSection)) {
                    throw new NoSuchElementException(String.format("Section \"%s\" cannot be found in \"%s\"", sectionName, Arrays.toString(split)));
                }

                section = (SimpleJsonSection) o;
            }
        } else if (!hasValue) {
            return (SimpleJsonSection) this.elements.get(split[0]);
        }

        return section;
    }

    /**
     * Obtains the element given the . split key
     *
     * @param key the key at which to find the element
     * @return the element
     */

    private Object getElement(String key) {
        String[] split = key.split(Pattern.quote("."));
        String finalKey = key;

        SimpleJsonSection section = this.findSection(split, true);
        if (section != this) {
            finalKey = split[split.length - 1];
        }

        // if all goes well, we have the final key at the
        // last element
        // try to get the value from the last child section
        // before the final key
        // if null, throw serverException
        Object element = section.elements.get(finalKey);
        if (element == null) {
            throw new NoSuchElementException(String.format("Key \"%s\" in your key \"%s\" cannot be found", finalKey, key));
        }

        return element;
    }

    /**
     * Concats the key to the given string from the given
     * entry.
     *
     * @param s     the base string
     * @param entry the entry to concat
     * @return the entry with the concatenated key
     */
    private Map.Entry<String, Object> concatKey(String s, Map.Entry<String, Object> entry) {
        return new Map.Entry<String, Object>() {
            @Override
            public String getKey() {
                return SimpleJsonSection.this.handlePath(s, entry.getKey());
            }

            @Override
            public Object getValue() {
                return entry.getValue();
            }

            @Override
            public Object setValue(Object value) {
                return entry.setValue(value);
            }

            @Override
            public boolean equals(Object o) {
                return entry.equals(o);
            }

            @Override
            public int hashCode() {
                return entry.hashCode();
            }
        };
    }

    /**
     * Yet another dumb class without any real purpose that is
     * made necessary simply for no apparent reason
     * <p>
     * since I am lazy I don't intend on adding any unnecessary
     * bs code that won't be used, so this really isn't a real
     * "map"
     * <p>
     * all these methods are self-explanatory, there is no use
     * for documenting them
     */

    public static class BasedMap<V> {
        /**
         * The lock which protects the map
         */
        private final ReadWriteLock lock = new ReentrantReadWriteLock();

        /**
         * The underlying map
         */
        private final LinkedHashMap<String, V> map = new LinkedHashMap<>();

        public void put(String key, V value) {
            Lock lock = this.lock.writeLock();
            lock.lock();
            try {
                this.map.put(key, value);
            } finally {
                lock.unlock();
            }
        }

        public V remove(String key) {
            Lock lock = this.lock.writeLock();
            lock.lock();
            try {
                return this.map.remove(key);
            } finally {
                lock.unlock();
            }
        }

        public V get(String key) {
            Lock lock = this.lock.readLock();
            lock.lock();
            try {
                return this.map.get(key);
            } finally {
                lock.unlock();
            }
        }

        public boolean containsKey(String key) {
            Lock lock = this.lock.readLock();
            lock.lock();
            try {
                return this.map.containsKey(key);
            } finally {
                lock.unlock();
            }
        }

        public Set<String> keySet() {
            Lock lock = this.lock.readLock();
            lock.lock();
            try {
                return this.map.keySet();
            } finally {
                lock.unlock();
            }
        }

        public Collection<V> values() {
            Lock lock = this.lock.readLock();
            lock.lock();
            try {
                return this.map.values();
            } finally {
                lock.unlock();
            }
        }

        public void forEach(BiConsumer<String, V> consumer) {
            Lock lock = this.lock.readLock();
            lock.lock();
            try {
                this.map.forEach(consumer);
            } finally {
                lock.unlock();
            }
        }

        public Set<Map.Entry<String, V>> entrySet() {
            Lock lock = this.lock.readLock();
            lock.lock();
            try {
                return this.map.entrySet();
            } finally {
                lock.unlock();
            }
        }
    }
}