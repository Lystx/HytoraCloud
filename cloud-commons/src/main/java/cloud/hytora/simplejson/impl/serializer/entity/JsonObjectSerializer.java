package cloud.hytora.simplejson.impl.serializer.entity;

import cloud.hytora.simplejson.elements.JsonEntity;
import cloud.hytora.simplejson.elements.object.JsonObject;
import cloud.hytora.simplejson.api.Json;
import cloud.hytora.simplejson.api.JsonSerializer;

import java.lang.reflect.Field;

public class JsonObjectSerializer extends JsonSerializer<JsonObject> {


    @Override
    public JsonObject deserialize(JsonEntity element, Field field, Json json, Class<?>... arguments) {
        return element.asJsonObject();
    }

    @Override
    public JsonEntity serialize(JsonObject obj, Json json, Field field) {
        return obj;
    }
}
