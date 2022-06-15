package cloud.hytora.driver.script.commands;

import cloud.hytora.driver.script.ScriptCommand;

import javax.annotation.Nonnull;
import java.util.Arrays;


public class VarScriptCommand implements ScriptCommand {

	@Override
	public void execute(@Nonnull String[] args, @Nonnull String input, @Nonnull String commandLine) {
		System.setProperty(args[0], String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
	}
}
