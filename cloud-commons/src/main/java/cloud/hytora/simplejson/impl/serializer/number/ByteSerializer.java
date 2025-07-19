package cloud.hytora.simplejson.impl.serializer.number;

import cloud.hytora.simplejson.elements.JsonEntity;
import cloud.hytora.simplejson.api.Json;
import cloud.hytora.simplejson.api.JsonSerializer;

import java.lang.reflect.Field;

public class ByteSerializer extends JsonSerializer<Byte> {

    @Override
    public Byte deserialize(JsonEntity element, Field field, Json json, Class<?>... arguments) {
        return element.asByte();
    }

    @Override
    public JsonEntity serialize(Byte obj, Json json, Field field) {
        return JsonEntity.valueOf(obj);
    }
}
