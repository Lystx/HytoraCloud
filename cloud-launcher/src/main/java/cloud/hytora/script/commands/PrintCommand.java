package cloud.hytora.script.commands;

import cloud.hytora.script.ScriptCommand;

public class PrintCommand implements ScriptCommand {

    @Override
    public void execute(String[] args, String input, String commandLine) {
        System.out.println(commandLine);
    }
}
