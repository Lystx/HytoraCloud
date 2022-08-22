package cloud.hytora.commands;

import cloud.hytora.common.logging.LogLevel;
import cloud.hytora.common.logging.Logger;
import cloud.hytora.common.logging.handler.LogEntry;
import cloud.hytora.script.api.IScript;
import cloud.hytora.script.api.IScriptCommand;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class LoggerCommand implements IScriptCommand {
    @Override
    public String getCommand() {
        return "log";
    }

    @Override
    public void execute(@NotNull String commandLine, @NotNull IScript script, @NotNull Collection<String> allLines) {
        String[] args = commandLine.split(" ");
        commandLine = commandLine.replace(args[0], "").trim();
        LogLevel level = LogLevel.fromName(args[0]);

        Logger.constantInstance().log(level, commandLine);
    }
}
