package cloud.hytora.simplejson;

import cloud.hytora.simplejson.api.*;
import cloud.hytora.simplejson.api.enums.JsonFormat;
import cloud.hytora.simplejson.impl.SimpleJson;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class JsonBuilder {

    /**
     * The format for printing
     */
    private JsonFormat format;

    /**
     * If nulls should be serialized
     */
    private boolean serializeNulls;

    /**
     * The amount of times a field of an object
     * will be serialized if its the same type as the class
     * (to prevent StackOverFlow)
     */
    private int innerClassSerialization;

    /**
     * If an object that no serializer was found for
     * should check for all subclasses if a serializer exists
     */
    private boolean checkSerializersForSubClasses;

    /**
     * If objects that are null should be provided as java-null
     * or as {@link cloud.hytora.simplejson.elements.JsonLiteral#NULL}
     */
    private boolean provideNulledObjectsAsRealNull;

    /**
     * If primitive arrays should be written like this : [1, 2, 3, 4, 5, 6]
     */
    private boolean writeArraysSingleLined;

    /**
     * Extra serializers
     */
    private final Map<Class<?>, JsonSerializer<?>> serializers;

    private final Collection<ConditionedSerializer<?>> conditionedSerializers;

    /**
     * Exclude strategies
     */
    private final Collection<ExcludeStrategy> excludeStrategies;

    public JsonBuilder() {
        this.format = JsonFormat.RAW;
        this.serializeNulls = true;
        this.writeArraysSingleLined = false;
        this.innerClassSerialization = 2;
        this.checkSerializersForSubClasses = true;
        this.provideNulledObjectsAsRealNull = true;
        this.serializers = new HashMap<>();
        this.conditionedSerializers = new ArrayList<>();
        this.excludeStrategies = new ArrayList<>();

    }

    /**
     * Sets the amount of times a field of an object
     * will be serialized if it's the same type as the class
     * (to prevent StackOverFlow)
     *
     * @param times the amount
     * @return current json
     */
    public JsonBuilder innerClassSerialization(int times) {
        this.innerClassSerialization = times;
        return this;
    }

    public JsonBuilder provideNulledObjectsAsLiteralNull() {
        this.provideNulledObjectsAsRealNull = false;
        return this;
    }

    public JsonBuilder provideNulledObjectsAsRealNull() {
        this.provideNulledObjectsAsRealNull = true;
        return this;
    }

    /**
     * Sets {@link JsonBuilder#writeArraysSingleLined} to true
     *
     * @return current builder
     */
    public JsonBuilder writeArraysSingleLined(boolean state) {
        this.writeArraysSingleLined = state;
        return this;
    }

    public JsonBuilder setExcludeStrategies(ExcludeStrategy... strategies) {
        this.excludeStrategies.addAll(Arrays.asList(strategies));
        return this;
    }

    /**
     * Sets the {@link JsonFormat} of this instance
     *
     * @param format the format
     * @return current json
     */
    public JsonBuilder format(JsonFormat format) {
        this.format = format;
        return this;
    }

    /**
     * Enables serializing nulls
     *
     * @return current json
     */
    public JsonBuilder serializeNulls(boolean state) {
        this.serializeNulls = state;
        return this;
    }

    /**
     * Enables checking for sub-class serializers
     *
     * @return current json
     */
    public JsonBuilder checkSerializersForSubClasses() {
        this.checkSerializersForSubClasses = true;
        return this;
    }



    /**
     * Adds a {@link JsonSerializer} to the cached ones
     *
     * @param serializer the serializer
     * @return current json
     */
    public <T> JsonBuilder addSerializer(Class<T> cls, JsonSerializer<T> serializer) {
        this.serializers.put(cls, serializer);
        return this;
    }


    /**
     * Adds a {@link JsonSerializer} to the cached ones
     *
     * @param serializer the serializer
     * @return current json
     */
    public <T> JsonBuilder addSerializer(Predicate<Class<?>> condition, JsonSerializer<T> serializer) {
        this.conditionedSerializers.add(new ConditionedSerializer<>(serializer, condition));
        return this;
    }

    /**
     * Builds this instance
     */
    public Json build(JsonSerializer<?>... serializers) {
        for (JsonSerializer serializer : serializers) {
            this.addSerializer(serializer.getTypeClass(), serializer);
        }
        return this.build();
    }
    /**
     * Builds this instance
     */
    public Json build() {
        Json json = new SimpleJson(format, serializeNulls, innerClassSerialization, checkSerializersForSubClasses, writeArraysSingleLined, this.serializers, this.conditionedSerializers, provideNulledObjectsAsRealNull);
        SimpleProvider.getInstance().setSerializerModule(json);
        SimpleProvider.getInstance().setParserModule(json);
        for (ExcludeStrategy strategy : excludeStrategies) {
            json.registerStrategy(strategy);
        }
        return  json;
    }
}
