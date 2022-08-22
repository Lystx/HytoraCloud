package cloud.hytora.driver.commands.parameter.defaults;

import cloud.hytora.common.util.Validation;
import cloud.hytora.driver.commands.parameter.CommandParameterType;

public class DefaultDoubleParameterType extends CommandParameterType<Double> {

    @Override
    public String label() {
        return "double";
    }

    @Override
    public Double resolve(String s) {
        if(Validation.INTEGER.matches(s)) return (double)Integer.valueOf(s);
        if(Validation.LONG.matches(s)) return (double)Long.valueOf(s);
        return Validation.DOUBLE.matches(s) ? Double.valueOf(s) : null;
    }

    @Override
    public Class<Double> typeClass() {
        return Double.class;
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
