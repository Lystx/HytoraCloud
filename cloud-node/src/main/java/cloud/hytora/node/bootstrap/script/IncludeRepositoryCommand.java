package cloud.hytora.node.bootstrap.script;

import cloud.hytora.node.bootstrap.library.Repository;
import cloud.hytora.script.api.IScript;
import cloud.hytora.script.api.IScriptCommand;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.Consumer;

@Data
public class IncludeRepositoryCommand implements IScriptCommand {

    private final Consumer<Repository> includer;

    @Override
    public String getCommand() {
        return "repo";
    }

    @Override
    public void execute(@NotNull String commandLine, @NotNull IScript script, @NotNull Collection<String> allLines) {
        String[] args = commandLine.split(" ");
        if (args.length == 2) {
            Repository repo = new Repository(args[0], args[1]);
            this.includer.accept(repo);
        }
    }
}
