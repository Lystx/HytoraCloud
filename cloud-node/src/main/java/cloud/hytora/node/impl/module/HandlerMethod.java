
package cloud.hytora.node.impl.module;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.lang.reflect.Method;

@Getter @RequiredArgsConstructor
public class HandlerMethod<T> {

    private final Object listener;
    private final Method method;
    private final Class<?> aClass;
    private final T annotation;

    @Setter
    private Object[] objects;
}
