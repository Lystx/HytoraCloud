package cloud.hytora.context.definition.reader;

import lombok.RequiredArgsConstructor;
import cloud.hytora.context.annotations.DefinedMethod;
import cloud.hytora.context.annotations.Configuration;
import cloud.hytora.context.definition.IDefinition;
import cloud.hytora.context.definition.MethodDefinition;
import cloud.hytora.context.utils.PackageScanner;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ConfigurationDefinitionReader implements IDefinitionReader {

    private final PackageScanner packageScanner;
    private final String packageName;

    @Override
    public Set<IDefinition> getDefinitions() {
        return packageScanner.findClasses(packageName)
                .stream()
                .filter(clazz -> clazz.isAnnotationPresent(Configuration.class))
                .map(Class::getDeclaredMethods)
                .flatMap(Arrays::stream)
                .filter(method -> method.isAnnotationPresent(DefinedMethod.class))
                .map(MethodDefinition::new)
                .collect(Collectors.toSet());
    }
}
