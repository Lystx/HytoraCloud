package cloud.hytora.context.definition.registry;

import cloud.hytora.context.definition.IDefinition;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DefaultDefinitionRegistry implements DefinitionRegistry {

    private Map<String, IDefinition> allCachedDefinitions = new ConcurrentHashMap<>();

    @Override
    public void registerDefinition(IDefinition iDefinition) {
        allCachedDefinitions.put(iDefinition.getName(), iDefinition);
    }

    @Override
    public Collection<IDefinition> getDefinitions(Class<?> clazz) {
        return getDefinitions()
                .stream()
                .filter(bd -> {
                    boolean result = clazz.isAssignableFrom(bd.getBeanClass());

                    if (!result)
                        for (Class<?> anInterface : clazz.getInterfaces()) {
                            if (anInterface.isAssignableFrom(bd.getBeanClass()))
                                return true;
                        }
                    return result;
                }).collect(Collectors.toSet());
    }

    @Override
    public Collection<IDefinition> getDefinitions() {
        return allCachedDefinitions.values();
    }

    @Override
    public IDefinition getDefinition(String name) {
        return allCachedDefinitions.get(name);
    }

    @Override
    public boolean containsDefinition(IDefinition iDefinition) {
        return allCachedDefinitions.containsValue(iDefinition);
    }

    @Override
    public Set<String> getDefinitionNames() {
        return allCachedDefinitions.keySet();
    }
}
