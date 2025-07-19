package cloud.hytora.simplejson.api.modules;

import cloud.hytora.simplejson.elements.JsonEntity;

public interface SerializerModule {

    /**
     * Checks if arrays should be written in a single line
     */
    boolean isWriteArraysSingleLined();

    /**
     * Checks if this json instance
     * checks for subclasses of serializers
     */
    boolean isCheckSerializersForSubClasses();

    /**
     * Checks if this json instance
     * checks for subclasses of serializers
     */
    boolean isProvideNulledObjectsAsRealNull();

    /**
     * Checks if nulled values should be serialized
     */
    boolean isSerializeNulls();

    /**
     * Parses an Object into a {@link JsonEntity}
     *
     * @param obj the object
     * @return the json element or null if an error occured
     */
    <T> JsonEntity toJson(T obj);

    /**
     * Creates a new object of a {@link JsonEntity} for a provided class
     *
     * @param json      the json element
     * @param typeClass the class of the object
     * @param <T>       the generic
     * @return created object
     */
    <T> T fromJson(JsonEntity json, Class<T> typeClass, Class<?>... arguments);
}
