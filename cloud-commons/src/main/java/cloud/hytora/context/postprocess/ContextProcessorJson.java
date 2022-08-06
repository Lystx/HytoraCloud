package cloud.hytora.context.postprocess;

import cloud.hytora.context.IApplicationContext;
import cloud.hytora.context.annotations.JsonContext;
import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ContextProcessorJson implements IPostProcessor {
    @Override
    public Object postProcessorBeforeInitialisation(String name, Object v, IApplicationContext context) {

        Class<?> clazz = v.getClass();
        for (Field declaredField : clazz.getDeclaredFields()) {

            if (!declaredField.isAnnotationPresent(JsonContext.class)) continue;




            try {
                JsonContext jsonContext = declaredField.getAnnotation(JsonContext.class);

                String file = jsonContext.file();
                String key = jsonContext.key();
                Document document = DocumentFactory.newJsonDocument(new File(file));


                Object value;

                if (declaredField.getType().isAssignableFrom(List.class)) {
                    Type type = ((ParameterizedType) declaredField.getGenericType()).getActualTypeArguments()[0];
                    Class<?> aClass = Class.forName(type.getTypeName());
                    value = new ArrayList<>(document.getBundle(key).toInstances(aClass));
                } else if (declaredField.getType().isAssignableFrom(Set.class)) {
                    Type type = ((ParameterizedType) declaredField.getGenericType()).getActualTypeArguments()[0];
                    Class<?> aClass = Class.forName(type.getTypeName());
                    value = new HashSet<>(document.getBundle(key).toInstances(aClass));
                } else
                    value = document.getInstance(key, declaredField.getType());

                declaredField.setAccessible(true);
                declaredField.set(v, value);

            } catch (Exception e) {
                throw new RuntimeException(e); //TODO Custom Exception
            }

        }

        return v;
    }

    @Override
    public Object postProcessorAfterInitialisation(String name, Object value, IApplicationContext context) {
        return value;
    }
}
