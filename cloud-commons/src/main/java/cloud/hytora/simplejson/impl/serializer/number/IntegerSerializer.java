package cloud.hytora.simplejson.impl.serializer.number;

import cloud.hytora.simplejson.elements.JsonEntity;
import cloud.hytora.simplejson.api.Json;
import cloud.hytora.simplejson.api.JsonSerializer;

import java.lang.reflect.Field;

public class IntegerSerializer extends JsonSerializer<Integer> {

    @Override
    public Integer deserialize(JsonEntity element, Field field, Json json, Class<?>... arguments) {

        return element.asInt();
    }

    @Override
    public JsonEntity serialize(Integer obj, Json json, Field field) {
        return JsonEntity.valueOf(obj);
    }
}
