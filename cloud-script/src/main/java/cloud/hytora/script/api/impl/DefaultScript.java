package cloud.hytora.script.api.impl;

import cloud.hytora.common.task.Task;
import cloud.hytora.script.ScriptSyntax;
import cloud.hytora.script.api.*;

import java.util.List;
import java.nio.file.Path;
import java.util.Collection;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@Data
public class DefaultScript implements IScript {

    private final Path scriptPath;
    private final IScriptLoader loader;
    private final List<String> allLines;
    private final Collection<IScriptTask> allTasks;
    private final Map<String, Map.Entry<Integer, IScriptCommand>> commandsPerLine;
    private final Map<String, Map.Entry<Integer, IScriptDecision>> decisionsPerLine;
    private final Map<String, String> variables = new HashMap<>();

    @Override
    public void putVariable(@NotNull String name, @NotNull String value) {
        this.variables.put(name, value);
        System.setProperty(name.trim(), value.trim());
    }


    public @NotNull String replaceVariables(@NotNull String line) {
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String varName = entry.getKey();
            String varValue = entry.getValue();


            String toReplace = ScriptSyntax.VARIABLE_BRACKET_RIGHT + varName + ScriptSyntax.VARIABLE_BRACKET_LEFT;
            if (line.contains(toReplace)) {
                line = line.replace(toReplace, varValue);
            }
        }
        return line;
    }

    @Override
    public @Nullable String getVariable(@NotNull String name) {
        return this.variables.get(name);
    }


    @Override
    public Task<Void> executeAsync() {
        return Task.callAsync(() -> {

            for (String line : this.allLines) {
                //System.out.println("Executing script line " + line);

                Map.Entry<Integer, IScriptCommand> entry = this.commandsPerLine.get(line);
                if (entry == null) {
                    Map.Entry<Integer, IScriptDecision> decisionEntry = this.decisionsPerLine.get(line.trim());
                    if (decisionEntry != null) {
                        IScriptDecision decision = decisionEntry.getValue();
                        if (decision.getChecker().supply(this)) {
                            decision.executeTrue(this);
                        } else {
                            decision.executeFalse(this);
                        }
                    }
                    continue;
                }




                IScriptCommand command = entry.getValue();
                Integer cursorPosition = entry.getKey();


                //Logger.constantInstance().debug("Executing cursor line " + key + "...");
                command.execute(ScriptSyntax.formatCommandLine(this, command, line), this, this.allLines);
                // Logger.constantInstance().debug("Executed cursor line " + key);
            }
            return null;
        });
    }

    @Override
    public void execute() {
    }
}
