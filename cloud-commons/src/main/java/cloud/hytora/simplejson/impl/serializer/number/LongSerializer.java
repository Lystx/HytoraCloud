package cloud.hytora.simplejson.impl.serializer.number;

import cloud.hytora.simplejson.elements.JsonEntity;
import cloud.hytora.simplejson.api.Json;
import cloud.hytora.simplejson.api.JsonSerializer;

import java.lang.reflect.Field;

public class LongSerializer extends JsonSerializer<Long> {

    @Override
    public Long deserialize(JsonEntity element, Field field, Json json, Class<?>... arguments) {
        return element.asLong();
    }

    @Override
    public JsonEntity serialize(Long obj, Json json, Field field) {
        return JsonEntity.valueOf(obj);
    }
}
