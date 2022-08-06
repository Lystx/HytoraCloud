package cloud.hytora.context.definition;

import java.lang.reflect.Method;

public interface IDefinition {
    String getName();
    Class<?> getBeanClass();
    default Method getFactoryMethod() {
        return null;
    }
}
