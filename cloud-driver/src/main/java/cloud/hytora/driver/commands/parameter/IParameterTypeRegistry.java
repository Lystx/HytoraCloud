package cloud.hytora.driver.commands.parameter;

import cloud.hytora.driver.common.IRegistry;

public interface IParameterTypeRegistry extends IRegistry<String, CommandParameterType> {


    CommandParameterType get(Class<?> c);

    /**
     * Registers a param definition type
     *
     * @param paramDefTypes The type objects
     */
    @Override
    boolean register(CommandParameterType... paramDefTypes);

    /**
     * Unregisters a param definition type
     *
     * @param paramDefTypes The type objects
     */
    @Override
    boolean unregister(CommandParameterType... paramDefTypes);

}
