package cloud.hytora.simplejson.api;

import cloud.hytora.simplejson.api.modules.ParserModule;
import cloud.hytora.simplejson.api.modules.SerializerModule;
import cloud.hytora.simplejson.api.config.JsonConfig;
import cloud.hytora.simplejson.elements.JsonEntity;
import cloud.hytora.simplejson.api.enums.JsonFormat;

import java.io.File;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.Map;

public interface Json extends SerializerModule, ParserModule {

    /**
     * Registers a new {@link JsonSerializer} for a given {@link Class}
     *
     * @param typeClass  the class of the object
     * @param serializer the serializer
     * @param <T>        the generic
     */
    <T>
    void registerSerializer(Class<T> typeClass, JsonSerializer<T> serializer);

    /**
     * Registers a new {@link ExcludeStrategy} for this json instance
     *
     * @param strategy the strategy
     */
    void registerStrategy(ExcludeStrategy strategy);

    /**
     * Parses an Object into a {@link JsonEntity}
     *
     * @param obj the object
     * @return the json element or null if an error occured
     */
    <T> JsonEntity toJson(T obj);

    void toJson(JsonEntity entity, Writer writer) throws Exception;

    /**
     * Creates a new object of a {@link JsonEntity} for a provided class
     *
     * @param json      the json element
     * @param typeClass the class of the object
     * @param <T>       the generic
     * @return created object
     */
    <T> T fromJson(JsonEntity json, Class<T> typeClass, Class<?>... arguments);

    /**
     * Creates a new object of a {@link String} for a provided class
     *
     * @param json      the string
     * @param typeClass the class of the object
     * @param arguments the arguments to provide for example for {@link java.util.List} or {@link Map}
     * @param <T>       the generic
     * @return created object
     */
    <T> T fromJson(String json, Class<T> typeClass, Class<?>... arguments);

    /**
     * Creates a new object of a {@link Reader} for a provided class
     *
     * @param reader    the reader
     * @param typeClass the class of the object
     * @param arguments the arguments to provide for example for {@link java.util.List} or {@link Map}
     * @param <T>       the generic
     * @return created object
     */
    <T> T fromJson(Reader reader, Class<T> typeClass, Class<?>... arguments);

    /**
     * Gets all registered {@link JsonSerializer} for their
     * given Wrapper {@link Class}
     */
    Map<Class<?>, JsonSerializer<?>> getRegisteredSerializers();

    Collection<ConditionedSerializer<?>> getConditionedSerializers();

    /**
     * The format of the json instance
     */
    JsonFormat getFormat();

    /**
     * Loads the given config into memory, given the path
     * as a File object.
     *
     * <p>Implementations are recommended to cache configs
     * loaded by this method in order to keep copies
     * sane and consistent. You can expect that configs
     * loaded from the same path will be {@code ==} to each
     * other.</p>
     *
     * @param file the file from which to laod the config
     * @return the loaded config memory representation
     */
    JsonConfig loadConfig(File file);

}
