package cloud.hytora.context.definition.reader;

import lombok.RequiredArgsConstructor;
import cloud.hytora.context.definition.IDefinition;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CompositeDefinitionReader implements IDefinitionReader {
    private final List<IDefinitionReader> iDefinitionReaders;

    @Override
    public Set<IDefinition> getDefinitions() {
        return iDefinitionReaders
                .stream()
                .map(IDefinitionReader::getDefinitions)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }
}
