package cloud.hytora.driver.commands.parameter.defaults;

import cloud.hytora.driver.commands.parameter.CommandParameterType;

public class DefaultStringParameterType extends CommandParameterType<String> {

    @Override
    public String label() {
        return "string";
    }

    @Override
    public String resolve(String s) {
        return s;
    }

    @Override
    public Class<String> typeClass() {
        return String.class;
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
