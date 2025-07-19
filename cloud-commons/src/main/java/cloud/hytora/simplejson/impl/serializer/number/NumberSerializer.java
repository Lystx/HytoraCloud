package cloud.hytora.simplejson.impl.serializer.number;

import cloud.hytora.simplejson.elements.JsonEntity;
import cloud.hytora.simplejson.elements.JsonNumber;
import cloud.hytora.simplejson.api.Json;
import cloud.hytora.simplejson.api.JsonSerializer;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;

public class NumberSerializer extends JsonSerializer<Number> {

    @Override
    public Number deserialize(JsonEntity element, Field field, Json json, Class<?>... arguments) {
        if (element.isDouble()) {
            return element.asDouble();
        } else if (element.isInt()) {
            return element.asInt();
        } else if (element.isLong()) {
            return element.asLong();
        } else if (element.isShort()) {
            return element.asShort();
        } else if (element.isByte()) {
            return element.asByte();
        } else if (element.isFloat()) {
            return element.asFloat();
        }
        return -1;
    }

    @Override
    public JsonEntity serialize(Number obj, Json json, Field field) {
        if (obj instanceof AtomicInteger) {
            AtomicInteger atomicInteger = (AtomicInteger)obj;
            return new JsonNumber(atomicInteger.get());
        }
        return new JsonNumber((Double) obj);
    }
}
