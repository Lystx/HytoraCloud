package cloud.hytora.simplejson.impl.strategy;

import cloud.hytora.simplejson.api.ExcludeStrategy;
import cloud.hytora.simplejson.api.annotation.JsonExcludeField;
import cloud.hytora.simplejson.api.annotation.JsonExcludeIfNull;

import java.lang.reflect.Field;

public class ExcludeIfAnnotatedStrategy implements ExcludeStrategy {


    @Override
    public boolean shouldSkipField(Field field, Object obj) {
        JsonExcludeField annotation = field.getAnnotation(JsonExcludeField.class);
        return annotation != null;
    }

    @Override
    public boolean shouldSkipClass(Class<?> cls) {
        return false;
    }
}
