package cloud.hytora.context.definition;

import cloud.hytora.context.annotations.Qualifier;
import cloud.hytora.context.exceptions.MultiplyAnnotationTypeException;
import cloud.hytora.context.exceptions.WrongQualifierValueException;

import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ConstructorDefinition implements IDefinition {

    private final String beanName;
    private final Class<?> beanClass;

    public ConstructorDefinition(Class<?> beanClass) {
        this.beanClass = beanClass;

        Annotation[] annotations = beanClass.getAnnotations();

        List<Qualifier> qualifierAnnotations = Arrays.stream(annotations)
                .filter(annotation -> annotation.annotationType().isAssignableFrom(Qualifier.class))
                .map(annotation -> (Qualifier) annotation)
                .collect(Collectors.toList());

        if (qualifierAnnotations.size() > 1)
            throw new RuntimeException(new MultiplyAnnotationTypeException(Qualifier.class.getSimpleName()));

        if (qualifierAnnotations.size() == 1) {
            Qualifier annotation = qualifierAnnotations.get(0);
            String value = annotation.value();
            if (value.isEmpty())
                throw new RuntimeException(new WrongQualifierValueException(annotation.getClass().toString()));
            beanName = Introspector.decapitalize(annotation.value());
        } else
            this.beanName = Introspector.decapitalize(beanClass.getSimpleName());
    }

    @Override
    public String getName() {
        return beanName;
    }

    @Override
    public Class<?> getBeanClass() {
        return beanClass;
    }
}
