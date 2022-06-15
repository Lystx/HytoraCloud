package cloud.hytora.driver.script.commands;

import cloud.hytora.driver.script.ScriptCommand;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RunJavaScriptCommand implements ScriptCommand {

    @Override
    public void execute(String[] args, String input, String commandLine) {
        String cls = args[0];
        try {
            Class<?> _class = Class.forName(cls);
            for (int i = 1; i < args.length; i++) {
                String methodName = args[i].replace("()", "");
                Method method = _class.getMethod(methodName);
                method.setAccessible(true);
                method.invoke(_class);
            }
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
