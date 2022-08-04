package cloud.hytora.common.inject.base;

import cloud.hytora.common.inject.IBinder;
import cloud.hytora.common.inject.Injector;
import cloud.hytora.common.inject.annotation.Inject;
import lombok.Getter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

public class SimpleInjector implements Injector {

    //The static instance
    public static SimpleInjector instance = new SimpleInjector();

    //The cached values
    @Getter public final Map<Class<?>, Object> registerInjectors;
    @Getter public final Map<String, Object> namedInjectors;
    @Getter public final Map<String, Class<?>> namedClassInjectors;

    public SimpleInjector() {
        instance = this;
        this.registerInjectors = new HashMap<>();
        this.namedInjectors = new HashMap<>();
        this.namedClassInjectors = new HashMap<>();
    }

    @Override
    public <T> IBinder<T> bind(Class<T> cls) {
        return new SimpleBinder<>(cls);
    }

    /**
     * Sets all Fields for a given class object
     *
     * @param instance the class instance
     * @param cls the class to get all fields of
     */
    public void injectFields(Object instance, Class<?> cls) {

        for (Field declaredField : cls.getDeclaredFields()) {
            declaredField.setAccessible(true);
            Inject annotation = declaredField.getAnnotation(Inject.class);
            if (annotation != null && !annotation.value().trim().isEmpty()) {

                Object obj = namedInjectors.get(annotation.value());
                Class<?> instanceClass = obj == null ? declaredField.getType() : namedClassInjectors.get(annotation.value());

                try {
                    declaredField.set(instance, getInstance(instanceClass));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            } else if (declaredField.getAnnotation(Inject.class) != null) {
                Class<?> type = declaredField.getType();
                try {
                    declaredField.set(instance, getInstance(type));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();

                }
            }
        }
    }

    @Override
    public <T> T getInstance(Class<T> cls) {

        Object object = registerInjectors.get(cls);
        if (object != null) {
            this.injectFields(object, cls);
            return (T) object;
        } else {
            boolean found = false;
            T value = null;
            for (Constructor<?> declaredConstructor : cls.getDeclaredConstructors()) {
                if (!declaredConstructor.isAnnotationPresent(Inject.class)) {
                    continue;
                }
                found = true;
                Class<?>[] types = declaredConstructor.getParameterTypes();
                Object[] args = new Object[types.length];

                for (int i = 0; i < declaredConstructor.getParameters().length; i++) {
                    Parameter parameter = declaredConstructor.getParameters()[i];
                    Inject annotation = parameter.getAnnotation(Inject.class);
                    Class<?> type = parameter.getType();
                    if (annotation == null) {
                        Object instance = getInstance(type);
                        args[i] = instance;
                    } else {
                        String name = annotation.value();
                        if (!name.trim().isEmpty()) {

                            Object obj = namedInjectors.get(name);

                            args[i] = obj;
                        }
                    }
                }
                try {
                    value = (T) declaredConstructor.newInstance(args);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }

            }
            if (!found) {
                throw new IllegalStateException("Tried to get instance for " + cls + " but was neither registered nor did the provided class could pass on any constructors with the @" + Inject.class.getSimpleName() + "-Annotation!");
            }
            return value;
        }
    }

}
