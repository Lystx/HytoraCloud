package cloud.hytora.simplejson.impl.serializer.base;

import cloud.hytora.simplejson.elements.JsonEntity;
import cloud.hytora.simplejson.elements.JsonString;
import cloud.hytora.simplejson.api.Json;
import cloud.hytora.simplejson.api.JsonSerializer;

import java.lang.reflect.Field;

public class EnumSerializer extends JsonSerializer<Enum> {

    @Override
    public Enum deserialize(JsonEntity element, Field field, Json json, Class<?>... arguments) {
        try {
            Class enumClass = field.getType();
            return Enum.valueOf(enumClass, element.asString());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public JsonEntity serialize(Enum obj, Json json, Field field) {
        return new JsonString(obj.name());
    }
}
