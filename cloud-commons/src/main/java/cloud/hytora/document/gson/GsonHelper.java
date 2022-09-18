package cloud.hytora.document.gson;

import cloud.hytora.common.misc.ReflectionUtils;
import cloud.hytora.document.gson.adapter.*;
import cloud.hytora.http.ProtocolAddress;
import com.google.gson.*;
import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import cloud.hytora.common.collection.pair.Pair;
import cloud.hytora.common.misc.BukkitReflectionSerializationUtils;
import cloud.hytora.document.Bundle;
import cloud.hytora.document.Document;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public final class GsonHelper {

    public static final Gson DEFAULT_GSON, PRETTY_GSON;
    public static final TypeAdapter<Number> NUMBER_ADAPTER;

    static {
        GsonBuilder builder = new GsonBuilder()
                .serializeNulls()
                .disableHtmlEscaping()
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes fieldAttributes) {
                        return fieldAttributes.getAnnotation(ExcludeJsonField.class) != null;
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> aClass) {
                        return aClass.getAnnotation(ExcludeJsonField.class) != null;
                    }
                })
                .registerTypeAdapterFactory(GsonTypeAdapter.newPredictableFactory(BukkitReflectionSerializationUtils::isSerializable, new BukkitReflectionSerializableTypeAdapter()))
                .registerTypeAdapterFactory(GsonTypeAdapter.newTypeHierarchyFactory(Document.class, new DocumentTypeAdapter()))
                .registerTypeAdapterFactory(GsonTypeAdapter.newTypeHierarchyFactory(ProtocolAddress.class, new AddressTypeAdapter()))
                .registerTypeAdapterFactory(GsonTypeAdapter.newTypeHierarchyFactory(Bundle.class, new BundleTypeAdapter()))
                .registerTypeAdapterFactory(GsonTypeAdapter.newTypeHierarchyFactory(Pair.class, new PairTypeAdapter()))
                .registerTypeAdapterFactory(GsonTypeAdapter.newTypeHierarchyFactory(Class.class, new ClassTypeAdapter()))
                .registerTypeAdapterFactory(GsonTypeAdapter.newTypeHierarchyFactory(OffsetDateTime.class, new OffsetDateTimeTypeAdapter()));

        DEFAULT_GSON = builder.create();
        PRETTY_GSON = builder.setPrettyPrinting().create();
        NUMBER_ADAPTER = new GsonTypeAdapter<Number>() {
            @Override
            public void write(@Nonnull Gson gson, @Nonnull JsonWriter writer, @Nonnull Number object) throws IOException {
                writer.value(object);
            }

            @Override
            public Number read(@Nonnull Gson gson, @Nonnull JsonReader reader) throws IOException {
                if (reader.peek() == JsonToken.NULL) {
                    reader.nextNull();
                    return null;
                }
                return reader.nextDouble();
            }
        }.toTypeAdapter(DEFAULT_GSON);
    }

    @Nonnull
    public static GsonEntry toGsonEntry(@Nullable Object value) {
        return new GsonEntry(toJsonElement(value));
    }

    @Nonnull
    public static JsonElement toJsonElement(@Nullable Object value) {
        return value instanceof JsonElement ? (JsonElement) value
                : value == null ? JsonNull.INSTANCE
                : value instanceof Number ? NUMBER_ADAPTER.toJsonTree((Number) value)
                : value instanceof Boolean ? TypeAdapters.BOOLEAN.toJsonTree((boolean) value)
                : value instanceof Character ? TypeAdapters.CHARACTER.toJsonTree((char) value)
                : DEFAULT_GSON.toJsonTree(value);
    }

    private GsonHelper() {
    }


    @Nullable
    public static Object unpackJsonElement(@Nullable JsonElement element) {
        if (element == null || element.isJsonNull())
            return null;
        if (element.isJsonObject())
            return convertJsonObjectToMap(element.getAsJsonObject());
        if (element.isJsonArray())
            return convertJsonArrayToStringList(element.getAsJsonArray());
        if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isNumber()) return primitive.getAsNumber();
            if (primitive.isString()) return primitive.getAsString();
            if (primitive.isBoolean()) return primitive.getAsBoolean();
        }
        return element;
    }

    @Nullable
    public static String convertJsonElementToString(@Nullable JsonElement element) {
        if (element == null || element.isJsonNull())
            return null;
        if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isString()) return primitive.getAsString();
            if (primitive.isNumber()) return primitive.getAsNumber() + "";
            if (primitive.isBoolean()) return primitive.getAsBoolean() + "";
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
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            map.put(entry.getKey(), unpackJsonElement(entry.getValue()));
        }
    }


    @Nonnull
    public static List<String> convertJsonArrayToStringList(@Nonnull JsonArray array) {
        List<String> list = new ArrayList<>(array.size());
        for (JsonElement element : array) {
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
    public static JsonArray convertIterableToJsonArray(@Nonnull Gson gson, @Nonnull Iterable<?> iterable) {
        JsonArray array = new JsonArray();
        iterable.forEach(object -> array.add(gson.toJsonTree(object)));
        return array;
    }

    @Nonnull
    public static JsonArray convertArrayToJsonArray(@Nonnull Gson gson, @Nonnull Object array) {
        JsonArray jsonArray = new JsonArray();
        ReflectionUtils.forEachInArray(array, object -> jsonArray.add(gson.toJsonTree(object)));
        return jsonArray;
    }

    public static void setDocumentProperties(@Nonnull Gson gson, @Nonnull JsonObject object, @Nonnull Map<String, Object> values) {
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            Object value = entry.getValue();

            if (value == null) {
                object.add(entry.getKey(), null);
            } else if (value instanceof JsonElement) {
                object.add(entry.getKey(), (JsonElement) value);
            } else if (value instanceof Iterable) {
                Iterable<?> iterable = (Iterable<?>) value;
                object.add(entry.getKey(), convertIterableToJsonArray(gson, iterable));
            } else if (value.getClass().isArray()) {
                object.add(entry.getKey(), convertArrayToJsonArray(gson, value));
            } else if (value instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) value;
                JsonObject newObject = new JsonObject();
                object.add(entry.getKey(), newObject);
                setDocumentProperties(gson, newObject, map);
            } else {
                object.add(entry.getKey(), gson.toJsonTree(value));
            }
        }
    }

    public static int getSize(@Nonnull JsonObject object) {
        try {
            return object.size();
        } catch (NoSuchMethodError ex) {
        }

        return object.entrySet().size();
    }

}
