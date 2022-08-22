package cloud.hytora.driver.commands.parameter;

import cloud.hytora.driver.commands.parameter.CommandParameterType;
import cloud.hytora.driver.commands.parameter.defaults.*;
import cloud.hytora.driver.common.DriverRegistryPool;

public class DefaultParameterTypeRegistry extends DriverRegistryPool<String, CommandParameterType> implements IParameterTypeRegistry {

    /**
     * Default param definitions
     */
    private static final CommandParameterType[] DEFAULT_PARAM_TYPES = new CommandParameterType[]{
            new DefaultStringParameterType(),
            new DefaultBooleanParameterType(),
            new DefaultDoubleParameterType(),
            new DefaultIntegerParameterType(),
            new DefaultLongParameterType()
    };

    public DefaultParameterTypeRegistry() {
        register(DEFAULT_PARAM_TYPES);
    }

    public CommandParameterType get(Class<?> c) {
        for (Object t : keyObjectMap.values()) {
            if (CommandParameterType.class.isAssignableFrom(t.getClass())) {
                CommandParameterType type = (CommandParameterType) t;
                if (type.typeClass().equals(c)) return type;
            }
        }
        return null;
    }

    /**
     * Registers a param definition type
     *
     * @param paramDefTypes The type objects
     */
    @Override
    public boolean register(CommandParameterType... paramDefTypes) {
        boolean r = false;
        for (CommandParameterType t : paramDefTypes) {
            r = register(t.label(), t);
        }
        return r;
    }

    /**
     * Unregisters a param definition type
     *
     * @param paramDefTypes The type objects
     */
    @Override
    public boolean unregister(CommandParameterType... paramDefTypes) {
        boolean r = false;
        for (CommandParameterType t : paramDefTypes) {
            r = unregister(t.label(), t);
        }
        return r;
    }

}
