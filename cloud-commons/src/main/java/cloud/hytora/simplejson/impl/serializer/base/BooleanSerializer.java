package cloud.hytora.simplejson.impl.serializer.base;

import cloud.hytora.simplejson.elements.JsonEntity;
import cloud.hytora.simplejson.elements.JsonLiteral;
import cloud.hytora.simplejson.api.Json;
import cloud.hytora.simplejson.api.JsonSerializer;

import java.lang.reflect.Field;

public class BooleanSerializer extends JsonSerializer<Boolean> {


    @Override
    public Boolean deserialize(JsonEntity element, Field field, Json json, Class<?>... arguments) {
        return element.asBoolean();
    }

    @Override
    public JsonEntity serialize(Boolean obj, Json json, Field field) {
        return obj ? JsonLiteral.TRUE : JsonLiteral.FALSE;
    }
}
