package cloud.hytora.simplejson.impl.serializer.number;

import cloud.hytora.simplejson.elements.JsonEntity;
import cloud.hytora.simplejson.api.Json;
import cloud.hytora.simplejson.api.JsonSerializer;

import java.lang.reflect.Field;

public class FloatSerializer extends JsonSerializer<Float> {

    @Override
    public Float deserialize(JsonEntity element, Field field, Json json, Class<?>... arguments) {
        return element.asFloat();
    }

    @Override
    public JsonEntity serialize(Float obj, Json json, Field field) {
        return JsonEntity.valueOf(obj);
    }
}
