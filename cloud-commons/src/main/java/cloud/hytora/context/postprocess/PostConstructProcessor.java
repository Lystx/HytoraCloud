package cloud.hytora.context.postprocess;

import lombok.SneakyThrows;
import cloud.hytora.context.annotations.Constructor;
import cloud.hytora.context.IApplicationContext;
import cloud.hytora.context.exceptions.MultiplyAnnotationTypeException;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PostConstructProcessor implements IPostProcessor {

    private final Map<String, Method> classes = new ConcurrentHashMap<>();

    @Override
    public Object postProcessorBeforeInitialisation(String name, Object value, IApplicationContext context) {
        Class<?> aClass = value.getClass();

        List<Method> methods = Arrays.stream(aClass.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(Constructor.class))
                .collect(Collectors.toList());

        if (methods.size() > 1)
            throw new RuntimeException(new MultiplyAnnotationTypeException(Constructor.class.getSimpleName()));

        if (methods.size() == 1) {
            if (methods.get(0).getParameterCount() != 0)
                throw new RuntimeException("Method PostConstruct must not have parameters [" + name.getClass().getSimpleName() + "]");
            classes.put(name, methods.get(0));
        }
        return value;
    }

    @Override
    @SneakyThrows
    public Object postProcessorAfterInitialisation(String name, Object value, IApplicationContext context) {

        if (classes.containsKey(name)) {
            Method method = classes.get(name);
            method.setAccessible(true);
            method.invoke(value);
        }

        return value;
    }
}
