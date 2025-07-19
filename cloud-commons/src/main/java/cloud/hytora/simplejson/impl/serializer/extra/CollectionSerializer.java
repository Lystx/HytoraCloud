package cloud.hytora.simplejson.impl.serializer.extra;

import cloud.hytora.simplejson.elements.JsonArray;
import cloud.hytora.simplejson.elements.JsonEntity;
import cloud.hytora.simplejson.exceptions.JsonDeserializeException;
import cloud.hytora.simplejson.api.Json;
import cloud.hytora.simplejson.api.JsonSerializer;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;

public class CollectionSerializer extends JsonSerializer<Collection> {


    @Override
    public Collection deserialize(JsonEntity element, Field field, Json json, Class<?>... arguments) {

        Class<?> typeClass;

        if (arguments.length != 0) {
            typeClass = arguments[0];
        } else {
            if (field != null) {
                ParameterizedType stringListType = (ParameterizedType) field.getGenericType();
                typeClass = (Class<?>) stringListType.getActualTypeArguments()[0];
            } else {
                typeClass = null;
            }
        }

        Collection Collection = new ArrayList<>();
        for (JsonEntity jsonEntity : element.asJsonArray()) {
            Object o = jsonEntity.asObject();
            if (o == null) {
                if (typeClass == null) {
                    throw new JsonDeserializeException("Tried to deserialize Collection but couldn't find ClassType of Collection! Maybe try to parse arguments to the Json#fromJson Method!");
                }
                Collection.add(json.fromJson(jsonEntity, typeClass));
            } else {
                Collection.add(o);
            }
        }
        return Collection;
    }

    @Override
    public JsonEntity serialize(Collection obj, Json json, Field field) {
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
