package cloud.hytora.driver.script.commands;

import cloud.hytora.driver.script.ScriptCommand;
import cloud.hytora.driver.script.ScriptLoader;

import java.io.IOException;
import java.nio.file.Paths;

public class RunScriptCommand implements ScriptCommand {

    @Override
    public void execute(String[] args, String input, String commandLine) {

        try {
            if (input.contains(".script")) {
                String s = input.split(".script")[1];

                if (s.endsWith("as resource")) {
                    ScriptLoader.getInstance().executeScriptFromResource(args[0]);
                } else {
                    ScriptLoader.getInstance().executeScript(Paths.get(String.join(" ", args)));
                }
            } else {
                System.out.println("Can't run Script with no declaration!");
                System.out.println("Remember scripts have to be in a '.script' file-format!");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
