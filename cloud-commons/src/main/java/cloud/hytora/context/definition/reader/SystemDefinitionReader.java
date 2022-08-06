package cloud.hytora.context.definition.reader;

import cloud.hytora.context.definition.IDefinition;
import cloud.hytora.context.definition.ConstructorDefinition;
import cloud.hytora.context.postprocess.ContextProcessorCache;
import cloud.hytora.context.postprocess.ContextProcessorJson;
import cloud.hytora.context.postprocess.PostConstructProcessor;

import java.util.HashSet;
import java.util.Set;

public class SystemDefinitionReader implements IDefinitionReader {
    @Override
    public Set<IDefinition> getDefinitions() {

        Set<IDefinition> postProcessors = new HashSet<>();
        postProcessors.add(new ConstructorDefinition(ContextProcessorCache.class));
        postProcessors.add(new ConstructorDefinition(ContextProcessorJson.class));
        postProcessors.add(new ConstructorDefinition(PostConstructProcessor.class));

        return postProcessors;
    }
}
