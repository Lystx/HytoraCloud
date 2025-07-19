package cloud.hytora.simplejson.impl.serializer.number;

import cloud.hytora.simplejson.elements.JsonEntity;
import cloud.hytora.simplejson.api.Json;
import cloud.hytora.simplejson.api.JsonSerializer;

import java.lang.reflect.Field;

public class ShortSerializer extends JsonSerializer<Short> {

    @Override
    public Short deserialize(JsonEntity element, Field field, Json json, Class<?>... arguments) {
        return element.asShort();
    }

    @Override
    public JsonEntity serialize(Short obj, Json json, Field field) {
        return JsonEntity.valueOf(obj);
    }
}
