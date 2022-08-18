package cloud.hytora.script.api.impl;

import cloud.hytora.common.logging.Logger;
import cloud.hytora.script.api.IScript;
import cloud.hytora.script.api.IScriptCommand;
import cloud.hytora.script.api.IScriptLoader;
import cloud.hytora.script.api.IScriptTask;

import java.nio.file.Path;
import java.util.Collection;

import lombok.Data;

import java.util.Map;

@Data
public class DefaultScript implements IScript {

    private final Path scriptPath;
    private final IScriptLoader loader;
    private final Collection<String> allLines;
    private final Collection<IScriptTask> allTasks;
    private final Map<String, Map.Entry<Integer, IScriptCommand>> commandsPerLine;

    @Override
    public void execute() {
        for (String line : this.allLines) {
            //System.out.println("Executing script line " + line);

            Map.Entry<Integer, IScriptCommand> entry = this.commandsPerLine.get(line);
            if (entry == null) {
                continue;
            }

            IScriptCommand command = entry.getValue();
            Integer key = entry.getKey();

            //Logger.constantInstance().debug("Executing cursor line " + key + "...");
            command.execute(line.replaceFirst(command.getCommand() + " ", ""), this, this.allLines);
           // Logger.constantInstance().debug("Executed cursor line " + key);
        }
    }
}
