package cloud.hytora.document.json;

import cloud.hytora.common.collection.pair.Pair;
import cloud.hytora.common.misc.BukkitReflectionSerializationUtils;
import cloud.hytora.common.misc.ReflectionUtils;
import cloud.hytora.document.Bundle;
import cloud.hytora.document.Document;
import cloud.hytora.document.json.serializers.*;
import cloud.hytora.simplejson.JsonBuilder;
import cloud.hytora.simplejson.api.Json;
import cloud.hytora.simplejson.api.enums.JsonFormat;
import cloud.hytora.simplejson.elements.JsonArray;
import cloud.hytora.simplejson.elements.JsonEntity;
import cloud.hytora.simplejson.elements.JsonLiteral;
import cloud.hytora.simplejson.elements.object.JsonObject;
import cloud.hytora.simplejson.impl.strategy.ExcludeIfAnnotatedStrategy;
import cloud.hytora.simplejson.impl.strategy.ExcludeNullIfAnnotatedStrategy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public final class JsonHelper {

    public static final Json DEFAULT_JSON;

    static {

        JsonBuilder builder = new JsonBuilder()
                .serializeNulls(true)
                .writeArraysSingleLined(false)
                .provideNulledObjectsAsRealNull()
                .innerClassSerialization(2)
                .format(JsonFormat.SIMPLE)
                .setExcludeStrategies(
                        new ExcludeIfAnnotatedStrategy(),
                        new ExcludeNullIfAnnotatedStrategy()
                )
                .addSerializer(BukkitReflectionSerializationUtils::isSerializable, new BukkitReflectionSerializer())
                .addSerializer(Document.class, new DocumentSerializer())
                .addSerializer(Bundle.class, new BundleSerializer())
                .addSerializer(Pair.class, new PairSerializer())
                .addSerializer(OffsetDateTime.class, new OffSetDateTimeSerializer())
        ;

        DEFAULT_JSON = builder.build();

    }

    @Nonnull
    public static JsonEntry toJsonEntry(@Nullable Object value) {
        return new JsonEntry(toJsonElement(value));
    }

    @Nonnull
    public static JsonEntity toJsonElement(@Nullable Object value) {
        if (value instanceof JsonEntity) {
            return (JsonEntity) value;
        }
        return DEFAULT_JSON.toJson(value);
    }

    private JsonHelper() {
    }


    @Nullable
    public static Object unpackJsonElement(@Nullable JsonEntity element) {
        if (element == null || element.isNull())
            return null;
        if (element.isJsonObject())
            return convertJsonObjectToMap(element.asJsonObject());
        if (element.isArray())
            return convertJsonArrayToStringList(element.asJsonArray());
        if (element.isPrimitive()) {
            if (element.isString()) return element.asString();
            if (element.isNumber()) return element.asNumber() + "";
            if (element.isBoolean()) return element.asBoolean() + "";
        }
        return element;
    }

    @Nullable
    public static String convertJsonElementToString(@Nullable JsonEntity element) {
        if (element == null || element.isNull())
            return null;
        if (element.isPrimitive()) {
            if (element.isString()) return element.asString();
            if (element.isNumber()) return element.asNumber() + "";
            if (element.isBoolean()) return element.asBoolean() + "";
        }
        return element.toString();
    }

    @Nonnull
    public static Map<String, Object> convertJsonObjectToMap(@Nonnull JsonObject object) {
        Map<String, Object> map = new LinkedHashMap<>();
        convertJsonObjectToMap(object, map);
        return map;
    }

    public static void convertJsonObjectToMap(@Nonnull JsonObject object, @Nonnull Map<String, Object> map) {
        for (cloud.hytora.simplejson.elements.object.JsonEntry entry : object) {
            map.put(entry.getName(), unpackJsonElement(entry.getValue()));
        }
    }


    @Nonnull
    public static List<String> convertJsonArrayToStringList(@Nonnull JsonArray array) {
        List<String> list = new ArrayList<>(array.size());
        for (JsonEntity element : array) {
            list.add(convertJsonElementToString(element));
        }
        return list;
    }

    @Nonnull
    public static String[] convertJsonArrayToStringArray(@Nonnull JsonArray array) {
        String[] list = new String[array.size()];
        for (int i = 0; i < array.size(); i++) {
            list[i] = convertJsonElementToString(array.get(i));
        }
        return list;
    }

    @Nonnull
    public static JsonArray convertIterableToJsonArray(@Nonnull Json gson, @Nonnull Iterable<?> iterable) {
        JsonArray array = new JsonArray();
        iterable.forEach(object -> array.add(gson.toJson(object)));
        return array;
    }

    @Nonnull
    public static JsonArray convertArrayToJsonArray(@Nonnull Json gson, @Nonnull Object array) {
        JsonArray jsonArray = new JsonArray();
        ReflectionUtils.forEachInArray(array, object -> jsonArray.add(gson.toJson(object)));
        return jsonArray;
    }

    public static void setDocumentProperties(@Nonnull Json gson, @Nonnull JsonObject object, @Nonnull Map<String, Object> values) {
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            Object value = entry.getValue();

            if (value == null) {
                object.addProperty(entry.getKey(), JsonLiteral.NULL);
            } else if (value instanceof JsonEntity) {
                object.addProperty(entry.getKey(), (JsonEntity) value);
            } else if (value instanceof Iterable) {
                Iterable<?> iterable = (Iterable<?>) value;
                object.addProperty(entry.getKey(), convertIterableToJsonArray(gson, iterable));
            } else if (value.getClass().isArray()) {
                object.addProperty(entry.getKey(), convertArrayToJsonArray(gson, value));
            } else if (value instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) value;
                JsonObject newObject = new JsonObject();
                object.addProperty(entry.getKey(), newObject);
                setDocumentProperties(gson, newObject, map);
            } else {
                object.addProperty(entry.getKey(), gson.toJson(value));
            }
        }
    }

    public static int getSize(@Nonnull JsonObject object) {
        try {
            return object.size();
        } catch (NoSuchMethodError ex) {
        }

        return object.size();
    }

}
