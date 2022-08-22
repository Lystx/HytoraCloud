package cloud.hytora.driver.commands.parameter.defaults;

import cloud.hytora.common.util.Validation;
import cloud.hytora.driver.commands.parameter.CommandParameterType;

public class DefaultLongParameterType extends CommandParameterType<Long> {

    @Override
    public String label() {
        return "long";
    }

    @Override
    public Long resolve(String s) {
        if(Validation.INTEGER.matches(s)) return (long)Integer.valueOf(s);
        return Validation.LONG.matches(s) ? Long.valueOf(s) : null;
    }

    @Override
    public Class<Long> typeClass() {
        return Long.class;
    }

    @Override
    public boolean checkCustom(String arg, String s) {
        return true;
    }

    @Override
    public String handleCustomException(String s) {
        return null;
    }

}
