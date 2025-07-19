package cloud.hytora.simplejson.impl.serializer.extra;

import cloud.hytora.simplejson.elements.JsonArray;
import cloud.hytora.simplejson.elements.JsonEntity;
import cloud.hytora.simplejson.api.Json;
import cloud.hytora.simplejson.api.JsonSerializer;

import java.lang.reflect.Field;

public class IterableSerializer extends JsonSerializer<Iterable> {
    
    @Override
    public Iterable deserialize(JsonEntity element, Field field, Json json, Class<?>... arguments) {
        return element.asJsonArray();
    }

    @Override
    public JsonEntity serialize(Iterable obj, Json json, Field field) {
        JsonArray jsonArray = new JsonArray();
        for (Object o : obj) {

            JsonEntity entity = JsonEntity.valueOf(o);
            if (entity == null) {
                jsonArray.add(json.toJson(o));
            } else {
                jsonArray.add(entity);
            }
        }
        return jsonArray;
    }
}
