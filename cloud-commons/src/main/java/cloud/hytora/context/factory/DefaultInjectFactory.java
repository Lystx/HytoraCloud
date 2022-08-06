package cloud.hytora.context.factory;

import lombok.SneakyThrows;
import cloud.hytora.context.IApplicationContext;
import cloud.hytora.context.definition.IDefinition;
import cloud.hytora.context.definition.CustomDefinition;
import cloud.hytora.context.definition.registry.DefinitionRegistry;
import cloud.hytora.context.exceptions.MultipleInstanceException;
import cloud.hytora.context.exceptions.NoInstanceFoundException;
import cloud.hytora.context.exceptions.WrongQualifierValueException;
import cloud.hytora.context.postprocess.IPostProcessor;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


public class DefaultInjectFactory implements InjectFactory {

    private final DefinitionRegistry definitionRegistry;
    private final Map<String, Object> beans = new ConcurrentHashMap<>();

    public DefaultInjectFactory(IApplicationContext context, DefinitionRegistry definitionRegistry) {
        this.definitionRegistry = definitionRegistry;
        IDefinition iDefinition = new CustomDefinition(context.getClass());
        this.definitionRegistry.registerDefinition(iDefinition);
        this.beans.put(iDefinition.getName(), context);
    }

    @Override
    public <T> T getInstance(Class<T> beanClass) {
        Collection<T> beans = getStackedInstances(beanClass);
        if (beans.size() != 1)
            throw new RuntimeException(new MultipleInstanceException("Bean multiply or null [" + beanClass.getName() + "]"));
        return beans.stream().findFirst().get();
    }

    @Override
    public <T> Set<T> getStackedInstances(Class<T> beanClass) {
        return definitionRegistry.getDefinitions(beanClass)
                .stream()
                .map(IDefinition::getName)
                .map(this::get)
                .map(bean -> (T) bean)
                .collect(Collectors.toSet());
    }

    @Override
    public Object get(String beanName) {
        if (beans.containsKey(beanName))
            synchronized (this) {
                if (beans.containsKey(beanName))
                    return beans.get(beanName);
            }

        IDefinition iDefinition = definitionRegistry.getDefinition(beanName);

        if (iDefinition == null)
            throw new RuntimeException(new NoInstanceFoundException(beanName));

        Object bean = createInstance(iDefinition);

        if (bean != null)
            beans.put(beanName, bean);

        return bean;
    }

    @Override
    public void setInstance(String beanName, Object bean) {
        if (beans.containsKey(beanName)) {
            throw new RuntimeException(new WrongQualifierValueException("BeanName is already exits [" + beanName + "}"));
        }

        beans.put(beanName, bean);
    }

    private Object createInstance(IDefinition iDefinition) {
        Object bean = iDefinition.getFactoryMethod() == null ?
                createBeanByConstructor(iDefinition) :
                createBeanByFactoryMethod(iDefinition);

        if (bean == null)
            return null;

        if (!IPostProcessor.class.isAssignableFrom(iDefinition.getBeanClass())) {
            List<IPostProcessor> iPostProcessors = getPostProcessors();
            IApplicationContext context = getInstance(IApplicationContext.class);

            for (IPostProcessor iPostProcessor : iPostProcessors) {
                bean = iPostProcessor.postProcessorBeforeInitialisation(iDefinition.getName(), bean, context);
            }

            for (IPostProcessor iPostProcessor : iPostProcessors) {
                bean = iPostProcessor.postProcessorAfterInitialisation(iDefinition.getName(), bean, context);
            }
        }

        return bean;
    }

    private List<IPostProcessor> getPostProcessors() {
        return definitionRegistry.getDefinitions()
                .stream()
                .filter(bd -> IPostProcessor.class.isAssignableFrom(bd.getBeanClass()))
                .map(IDefinition::getName)
                .map(this::get)
                .map(bean -> (IPostProcessor) bean)
                .collect(Collectors.toList());
    }

    private Object createBeanByFactoryMethod(IDefinition iDefinition) {

        Method factoryMethod = iDefinition.getFactoryMethod();
        Object factoryBean = getInstance(factoryMethod.getDeclaringClass());

        Object[] values = resolveArguments(factoryMethod.getParameters());

        try {
            factoryMethod.setAccessible(true);
            return factoryMethod.invoke(factoryBean, values);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private Object createBeanByConstructor(IDefinition iDefinition) {
        Constructor<?>[] constructors = iDefinition.getBeanClass().getConstructors();

        if (constructors.length != 1) {
//            throw new RuntimeException(
//                    new MultiplyConstructorException("Bean must only one public constructor [" + beanDefinition.getBeanClass() + "]")
//            );
            return null;
        }

        Constructor<?> constructor = constructors[0];
        Object[] values = resolveArguments(constructor.getParameters());

        try {
            return constructor.newInstance(values);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private Object[] resolveArguments(Parameter[] parameters) {
        Object[] values = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            values[i] = resolveArgument(parameters[i]);
        }
        return values;
    }

    @SneakyThrows
    private Object resolveArgument(Parameter parameter) {

        if (parameter.getType().isAssignableFrom(List.class)) {
            Type type = ((ParameterizedType) parameter.getParameterizedType()).getActualTypeArguments()[0];
            Class<?> aClass = Class.forName(type.getTypeName());
            return new ArrayList<>(getStackedInstances(aClass));
        } else if (parameter.getType().isAssignableFrom(Set.class)) {
            Type type = ((ParameterizedType) parameter.getParameterizedType()).getActualTypeArguments()[0];
            Class<?> aClass = Class.forName(type.getTypeName());
            return new HashSet<>(getStackedInstances(aClass));
        } else {
            return getInstance(parameter.getType());
        }
    }
}
