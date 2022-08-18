package cloud.hytora.script.defaults;

import cloud.hytora.script.api.IScript;
import cloud.hytora.script.api.IScriptCommand;
import cloud.hytora.script.api.IScriptTask;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class DefaultRunCommand implements IScriptCommand {
    @Override
    public String getCommand() {
        return "run";
    }

    @Override
    public void execute(@NotNull String commandLine, @NotNull IScript script, @NotNull Collection<String> allLines) {
        String taskName = commandLine.replace(";", "").trim();
        IScriptTask task = script.getAllTasks()
                .stream()
                .filter(e -> e.getName().equalsIgnoreCase(taskName))
                .findFirst()
                .orElse(null);
        if (task == null) {
            throw new UnsupportedOperationException(commandLine + " tried to call task which does not exists");
        }

        task.executeTask(commandLine, script, allLines);
    }
}
