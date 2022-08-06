package cloud.hytora.context;

import cloud.hytora.context.definition.IDefinition;
import cloud.hytora.context.definition.CustomDefinition;
import cloud.hytora.context.definition.reader.*;
import cloud.hytora.context.definition.registry.DefinitionRegistry;
import cloud.hytora.context.definition.registry.DefaultDefinitionRegistry;
import cloud.hytora.context.factory.InjectFactory;
import cloud.hytora.context.factory.DefaultInjectFactory;
import cloud.hytora.context.utils.PackageScanner;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ApplicationContext implements IApplicationContext {

    @Getter
    private static ApplicationContext current;

    private final IDefinitionReader iDefinitionReader;
    private DefinitionRegistry definitionRegistry;
    private InjectFactory injectFactory;

    private final Object instance;

    public ApplicationContext(Object instance) {
        this.instance = instance;
        current = this;

        PackageScanner packageScanner = new PackageScanner();
        String packageName = instance.getClass().getPackage() == null ? "" : instance.getClass().getPackage().getName();

        List<IDefinitionReader> readers = new ArrayList<>();
        readers.add(new ConfigurationDefinitionReader(packageScanner, packageName));
        readers.add(new ComponentDefinitionReader(packageScanner, packageName));
        readers.add(new SystemDefinitionReader());

        iDefinitionReader = new CompositeDefinitionReader(readers);

        this.refresh();
    }

    @Override
    public <T> T getInstance(Class<T> beanClass) {
        return injectFactory.getInstance(beanClass);
    }

    @Override
    public <T> Set<T> getStackedInstances(Class<T> beanClass) {
        return injectFactory.getStackedInstances(beanClass);
    }

    @Override
    public Object get(String beanName) {
        return injectFactory.get(beanName);
    }

    @Override
    public void setInstance(String beanName, Object bean) {
        IDefinition iDefinition = new CustomDefinition(bean.getClass());
        definitionRegistry.registerDefinition(iDefinition);
        injectFactory.setInstance(beanName == null ? iDefinition.getName() : beanName, bean);
    }

    @Override
    public void refresh() {
        definitionRegistry = new DefaultDefinitionRegistry();
        Set<IDefinition> iDefinitions = iDefinitionReader.getDefinitions();

        iDefinitions.forEach(
                bd -> definitionRegistry.registerDefinition(bd)
        );
        injectFactory = new DefaultInjectFactory(this, definitionRegistry);

        setInstance(null, instance);

        definitionRegistry.getDefinitionNames().forEach(injectFactory::get);
    }
}
