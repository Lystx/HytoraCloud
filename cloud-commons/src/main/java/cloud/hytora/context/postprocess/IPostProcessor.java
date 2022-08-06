package cloud.hytora.context.postprocess;

import cloud.hytora.context.IApplicationContext;

public interface IPostProcessor {

    Object postProcessorBeforeInitialisation(String name, Object value, IApplicationContext context);
    Object postProcessorAfterInitialisation(String name, Object value, IApplicationContext context);
}
