package cloud.hytora.context.definition;

import lombok.RequiredArgsConstructor;

import java.beans.Introspector;

@RequiredArgsConstructor
public class CustomDefinition implements IDefinition {

    private final Class<?> beanClass;

    @Override
    public String getName() {
        return Introspector.decapitalize(beanClass.getSimpleName());
    }

    @Override
    public Class<?> getBeanClass() {
        return beanClass;
    }
}
