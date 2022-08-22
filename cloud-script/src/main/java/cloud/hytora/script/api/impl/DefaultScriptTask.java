package cloud.hytora.script.api.impl;

import cloud.hytora.script.ScriptSyntax;
import cloud.hytora.script.api.IScript;
import cloud.hytora.script.api.IScriptCommand;
import cloud.hytora.script.api.IScriptDecision;
import cloud.hytora.script.api.IScriptTask;
import lombok.AllArgsConstructor;

import java.util.Collection;
import java.util.Map;

@AllArgsConstructor
public class DefaultScriptTask implements IScriptTask {

    private final String name;
    private final Map<String, IScriptCommand> commands;
    private final Map<String, IScriptDecision> decisionsPerLine;

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void executeTask(String callerLine, IScript script, Collection<String> allLines) {

        for (String line : allLines) {
            //System.out.println("Executing script line " + line);

            IScriptCommand entry = this.commands.get(line.trim());
            if (entry == null) {
                IScriptDecision decision = this.decisionsPerLine.get(line.trim());
                if (decision != null) {
                    if (decision.getChecker().supply(script)) {
                        decision.executeTrue(script);
                    } else {
                        decision.executeFalse(script);
                    }
                }
                continue;
            }

            entry.execute(ScriptSyntax.formatCommandLine(script, entry, line.trim()), script, allLines);
        }

        /*
        for (Map.Entry<String, IScriptCommand> commandEntry : this.commands.entrySet()) {
            String key = commandEntry.getKey();
            IScriptCommand command = commandEntry.getValue();

            command.execute(ScriptSyntax.formatCommandLine(script, command, key), script, allLines);
        }*/
    }
}