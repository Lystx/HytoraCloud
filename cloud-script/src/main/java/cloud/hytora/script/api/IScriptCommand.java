package cloud.hytora.script.api;


import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface IScriptCommand {

	String getCommand();

	void execute(@NotNull String commandLine, @NotNull IScript script, @NotNull Collection<String> allLines);

}
