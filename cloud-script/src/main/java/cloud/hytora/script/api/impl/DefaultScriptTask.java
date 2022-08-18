package cloud.hytora.script.api.impl;

import cloud.hytora.script.api.IScript;
import cloud.hytora.script.api.IScriptCommand;
import cloud.hytora.script.api.IScriptTask;
import lombok.AllArgsConstructor;

import java.util.Collection;
import java.util.Map;

@AllArgsConstructor
public class DefaultScriptTask implements IScriptTask {

    private final String name;
    private final Map<String, IScriptCommand> commands;

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void executeTask(String callerLine, IScript script, Collection<String> allLines) {
        for (Map.Entry<String, IScriptCommand> commandEntry : this.commands.entrySet()) {
            String key = commandEntry.getKey();
            IScriptCommand command = commandEntry.getValue();
            command.execute(key.replaceFirst(command.getCommand() + " ", ""), script, allLines);
        }
    }
}