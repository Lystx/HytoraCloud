package cloud.hytora.simplejson.impl.serializer.base;

import cloud.hytora.simplejson.elements.JsonEntity;
import cloud.hytora.simplejson.elements.JsonLiteral;
import cloud.hytora.simplejson.elements.JsonString;
import cloud.hytora.simplejson.api.Json;
import cloud.hytora.simplejson.api.JsonSerializer;

import java.lang.reflect.Field;

public class StringSerializer extends JsonSerializer<String> {

    @Override
    public String deserialize(JsonEntity element, Field field, Json json, Class<?>... arguments) {
        if (element.isNull()) {
            return null;
        }
        return element.asString();
    }

    @Override
    public JsonEntity serialize(String obj, Json json, Field field) {
        return obj == null ? JsonLiteral.NULL : new JsonString(obj);
    }
}
