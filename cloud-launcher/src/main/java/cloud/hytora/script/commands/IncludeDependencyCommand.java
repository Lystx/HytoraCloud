package cloud.hytora.script.commands;

import cloud.hytora.dependency.Dependency;
import cloud.hytora.script.ScriptCommand;
import lombok.AllArgsConstructor;

import java.util.function.Consumer;

@AllArgsConstructor
public class IncludeDependencyCommand implements ScriptCommand {

    private final Consumer<Dependency> includer;

    @Override
    public void execute(String[] args, String input, String commandLine) {

        if (args.length >= 3) {
            Dependency dependency = new Dependency(args[0], args[1], args[2], args.length == 4 ? args[3] : "mvn");
            this.includer.accept(dependency);
        }
    }
}
