package cloud.hytora.simplejson.impl.strategy;

import cloud.hytora.simplejson.api.annotation.JsonExcludeIfNull;
import cloud.hytora.simplejson.api.ExcludeStrategy;

import java.lang.reflect.Field;

public class ExcludeNullIfAnnotatedStrategy implements ExcludeStrategy {


    @Override
    public boolean shouldSkipField(Field field, Object obj) {
        try {
            Object o = field.get(obj);
            JsonExcludeIfNull annotation = field.getAnnotation(JsonExcludeIfNull.class);
            if (o == null && annotation != null) {
                return true;
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean shouldSkipClass(Class<?> cls) {
        return false;
    }
}
