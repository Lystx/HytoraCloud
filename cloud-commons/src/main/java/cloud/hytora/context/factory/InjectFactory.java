package cloud.hytora.context.factory;

import java.util.Set;

public interface InjectFactory {
    <T> T getInstance(Class<T> typeClass);
    <T> Set<T> getStackedInstances(Class<T> typeClass);
    Object get(String name);

    void setInstance(String name, Object instance);

}
