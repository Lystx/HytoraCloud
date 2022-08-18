package cloud.hytora.script.defaults;

import cloud.hytora.script.api.IScript;
import cloud.hytora.script.api.IScriptCommand;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class DefaultPrintCommand implements IScriptCommand {

    @Override
    public String getCommand() {
        return "print";
    }

    @Override
    public void execute(@NotNull String commandLine, @NotNull IScript script, @NotNull Collection<String> allLines) {
        System.out.println(commandLine.trim().isEmpty() ? "" : commandLine.replaceFirst(" ", ""));
    }
}
