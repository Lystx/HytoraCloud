import cloud.hytora.script.api.IScript;
import cloud.hytora.script.api.IScriptLoader;
import cloud.hytora.script.api.impl.DefaultScriptLoader;
import cloud.hytora.script.defaults.DefaultPrintCommand;
import cloud.hytora.script.defaults.DefaultRunCommand;

import java.nio.file.Paths;

public class Test {

    public static void main(String[] args) {
        IScriptLoader loader = new DefaultScriptLoader();

        loader.registerCommand(new DefaultPrintCommand());
        loader.registerCommand(new DefaultRunCommand());

        IScript script = loader.loadScript(Paths.get("cloud.script"));
        if (script == null) {
            System.out.println("Couldn't load script!");
            return;
        }
        script.execute();
    }
}
