package cloud.hytora.simplejson.impl.serializer.number;

import cloud.hytora.simplejson.elements.JsonEntity;
import cloud.hytora.simplejson.api.Json;
import cloud.hytora.simplejson.api.JsonSerializer;

import java.lang.reflect.Field;

public class DoubleSerializer extends JsonSerializer<Double> {

    @Override
    public Double deserialize(JsonEntity element, Field field, Json json, Class<?>... arguments) {
        return element.asDouble();
    }

    @Override
    public JsonEntity serialize(Double obj, Json json, Field field) {
        return JsonEntity.valueOf(obj);
    }
}
