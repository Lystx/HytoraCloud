package cloud.hytora.simplejson.impl.serializer.entity;

import cloud.hytora.simplejson.elements.JsonArray;
import cloud.hytora.simplejson.elements.JsonEntity;
import cloud.hytora.simplejson.api.Json;
import cloud.hytora.simplejson.api.JsonSerializer;

import java.lang.reflect.Field;

public class JsonArraySerializer extends JsonSerializer<JsonArray> {


    @Override
    public JsonArray deserialize(JsonEntity element, Field field, Json json, Class<?>... arguments) {
        return element.asJsonArray();
    }

    @Override
    public JsonEntity serialize(JsonArray obj, Json json, Field field) {
        return obj;
    }
}
