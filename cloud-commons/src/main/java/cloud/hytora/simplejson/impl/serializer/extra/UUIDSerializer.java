package cloud.hytora.simplejson.impl.serializer.extra;

import cloud.hytora.simplejson.elements.JsonEntity;
import cloud.hytora.simplejson.elements.JsonString;
import cloud.hytora.simplejson.api.Json;
import cloud.hytora.simplejson.api.JsonSerializer;

import java.lang.reflect.Field;
import java.util.UUID;

public class UUIDSerializer extends JsonSerializer<UUID> {

    @Override
    public UUID deserialize(JsonEntity element, Field field, Json json, Class<?>... arguments) {
        return UUID.fromString(element.asString());
    }

    @Override
    public JsonEntity serialize(UUID obj, Json json, Field field) {
        return new JsonString(obj.toString());
    }
}
