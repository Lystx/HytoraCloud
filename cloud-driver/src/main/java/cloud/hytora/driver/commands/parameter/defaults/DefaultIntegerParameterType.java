package cloud.hytora.driver.commands.parameter.defaults;

import cloud.hytora.common.util.Validation;
import cloud.hytora.driver.commands.parameter.CommandParameterType;

public class DefaultIntegerParameterType extends CommandParameterType<Integer> {

    @Override
    public String label() {
        return "int";
    }

    @Override
    public Integer resolve(String s) {
        return Validation.INTEGER.matches(s) ? Integer.valueOf(s) : null;
    }

    @Override
    public Class<Integer> typeClass() {
        return Integer.class;
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
