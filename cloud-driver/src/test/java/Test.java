import cloud.hytora.driver.script.ScriptLoader;
import cloud.hytora.driver.script.commands.*;

import java.io.IOException;

public class Test {


    public static void main(String[] args) {
        ScriptLoader loader = ScriptLoader.getInstance();
        loader.registerCommand("var", new VarScriptCommand());
        loader.registerCommand("runScript", new RunScriptCommand());
        loader.registerCommand("log", new LogScriptCommand());
        loader.registerCommand("print", new PrintCommand());
        loader.registerCommand("javaExecute", new RunJavaScriptCommand());


        try {
            loader.executeScriptFromResource("test.hc");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
