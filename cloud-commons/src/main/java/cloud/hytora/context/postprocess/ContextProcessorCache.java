package cloud.hytora.context.postprocess;

import cloud.hytora.context.annotations.CacheContext;
import cloud.hytora.context.IApplicationContext;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class ContextProcessorCache implements IPostProcessor {
    @Override
    public Object postProcessorBeforeInitialisation(String name, Object v, IApplicationContext context) {

        Class<?> clazz = v.getClass();
        for (Field declaredField : clazz.getDeclaredFields()) {

            if (!declaredField.isAnnotationPresent(CacheContext.class)) continue;

            try {
                Object value;

                if (declaredField.getType().isAssignableFrom(List.class)) {
                    Type type = ((ParameterizedType) declaredField.getGenericType()).getActualTypeArguments()[0];
                    Class<?> aClass = Class.forName(type.getTypeName());
                    value = new ArrayList<>(context.getStackedInstances(aClass));
                } else if (declaredField.getType().isAssignableFrom(Set.class)) {
                    Type type = ((ParameterizedType) declaredField.getGenericType()).getActualTypeArguments()[0];
                    Class<?> aClass = Class.forName(type.getTypeName());
                    value = new HashSet<>(context.getStackedInstances(aClass));
                } else
                    value = context.getInstance(declaredField.getType());

                declaredField.setAccessible(true);
                declaredField.set(v, value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }

        return v;
    }

    @Override
    public Object postProcessorAfterInitialisation(String name, Object value, IApplicationContext context) {
        return value;
    }
}
