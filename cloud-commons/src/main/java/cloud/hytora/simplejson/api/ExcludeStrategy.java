package cloud.hytora.simplejson.api;

import java.lang.reflect.Field;

public interface ExcludeStrategy {

    boolean shouldSkipField(Field field, Object obj);

    boolean shouldSkipClass(Class<?> cls);
}
