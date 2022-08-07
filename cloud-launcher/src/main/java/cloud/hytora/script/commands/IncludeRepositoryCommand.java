
package cloud.hytora.script.commands;

import cloud.hytora.dependency.Repository;
import cloud.hytora.script.ScriptCommand;
import lombok.AllArgsConstructor;

import java.util.function.Consumer;

@AllArgsConstructor
public class IncludeRepositoryCommand implements ScriptCommand {

    private final Consumer<Repository> includer;

    @Override
    public void execute(String[] args, String input, String commandLine) {

        if (args.length == 1) {
            String repoString = args[0];
            String[] split = repoString.split("@");
            Repository repo = new Repository(split[0], split[1]);
            this.includer.accept(repo);
        }
    }
}
