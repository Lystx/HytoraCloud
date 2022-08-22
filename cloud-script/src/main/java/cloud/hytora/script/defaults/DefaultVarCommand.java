package cloud.hytora.script.defaults;

import cloud.hytora.script.api.IScript;
import cloud.hytora.script.api.IScriptCommand;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class DefaultVarCommand implements IScriptCommand {

    @Override
    public String getCommand() {
        return "var";
    }

    @Override
    public void execute(@NotNull String commandLine, @NotNull IScript script, @NotNull Collection<String> allLines) {
        if (!commandLine.contains("=")) {
            throw new IllegalStateException("Declaration of ScriptVariables need to be following schema: variable_name = variable_value");
        }
        String[] s = commandLine.trim().split("=");
        if (s.length <= 1) {
            throw new IllegalStateException("Cannot set Script value to null value!");
        }
        String valName = s[0].trim();


        if (script.getVariable(valName) != null) {
            throw new IllegalStateException("Immutable Value! Use 'modify' command to change value");
        }


        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < s.length; i++) {
            sb.append(s[i]);
            if (i != (s.length -1)) {
                sb.append(" ");
            }
        }

        script.putVariable(valName, sb.toString());
    }
}
