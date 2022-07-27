package cloud.hytora.driver.database;


import cloud.hytora.driver.networking.protocol.codec.buf.Bufferable;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public class SectionedDatabase {

    /**
     * The wrapped database
     */
    private final IDatabase database;

    /**
     * All cached sections
     */
    private final Map<Class<?>, DatabaseSection<?>> sections;

    public SectionedDatabase(IDatabase database) {
        this.database = database;
        this.sections = new HashMap<>();
    }

    /**
     * Registers a new {@link DatabaseSection} with a given name and a given
     * generic typeClass to (de-)serialize given objects
     *
     * @param name the name of this section (internally used for your collection names of database)
     * @param typeClass (the wrapper class of your object; don't supply interface classes!)
     * @param <T> the generic type that has to be a {@link Bufferable} object
     */
    public <T extends Bufferable> void registerSection(String name, Class<T> typeClass) {
        DatabaseSection<T> section = new DatabaseSection<T>(this.database,name, typeClass);
        this.sections.put(typeClass, section);
    }

    /**
     * Tries to retrieve a {@link DatabaseSection} by its specified type-class
     * In this method you can supply an interface class or a wrapper class
     * because the method searches for wrapper classes and then for classes
     * that can access the wrapper class (interfaces)
     *
     * @param typeClass the wrapper class or interface class
     * @param <T> the generic type that has to be a {@link Bufferable} object
     * @return found section or null
     */
    @SuppressWarnings("unchecked")
    public <T extends Bufferable> DatabaseSection<T> getSection(Class<T> typeClass) {
        if (sections.containsKey(typeClass)) {
            return (DatabaseSection<T>) sections.get(typeClass);
        } else {
            Class<?> aClass = sections.keySet().stream().filter(typeClass::isAssignableFrom).findFirst().orElse(null);
            return aClass == null ? null : (DatabaseSection<T>) sections.get(aClass);
        }
    }

}
