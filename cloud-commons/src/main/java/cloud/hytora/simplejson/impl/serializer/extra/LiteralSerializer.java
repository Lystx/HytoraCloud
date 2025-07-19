package cloud.hytora.simplejson.impl.serializer.extra;

import cloud.hytora.simplejson.elements.JsonEntity;
import cloud.hytora.simplejson.elements.JsonLiteral;
import cloud.hytora.simplejson.elements.JsonString;
import cloud.hytora.simplejson.api.Json;
import cloud.hytora.simplejson.api.JsonSerializer;

import java.lang.reflect.Field;

public class LiteralSerializer extends JsonSerializer<JsonLiteral> {


    @Override
    public JsonLiteral deserialize(JsonEntity element, Field field, Json json, Class<?>... arguments) {

        if (element.asString().equals("null") || element.equals(JsonLiteral.NULL)) {
            return (JsonLiteral) JsonLiteral.NULL;
        }
        if (element.asString().equalsIgnoreCase("true") || element.equals(JsonLiteral.TRUE)) {
            return (JsonLiteral) JsonLiteral.TRUE;
        }
        if (element.asString().equalsIgnoreCase("false") || element.equals(JsonLiteral.FALSE)) {
            return (JsonLiteral) JsonLiteral.FALSE;
        }
        return null;
    }

    @Override
    public JsonEntity serialize(JsonLiteral obj, Json json, Field field) {
        return new JsonString(obj.toString());
    }
}
