package cloud.hytora.driver.command.annotation.data;

import cloud.hytora.driver.command.CommandScope;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

@AllArgsConstructor @Getter
public class RegisteredCommand {

	private final String[] names;
	private final String path;
	private final String[] allPaths;
	private final String permission;
	private final String description;
	private final String mainDescription;
	private final CommandScope scope;
	private final List<RegisteredCommandArgument> arguments;
	private final Method method;
	private final Object instance;


	@Nonnull
	public RegisteredCommandArgument getArgument(@Nonnull String name) {
		for (RegisteredCommandArgument argument : arguments) {
			if (argument.getName().equalsIgnoreCase(name))
				return argument;
		}
		throw new IllegalStateException("No such argument: " + name + " in " + this);
	}

	@Override
	public String toString() {
		return "[" + "name=" + Arrays.toString(names) + " path='" + path + '\'' + " permission='" + permission + "' scope=" + scope + ']';
	}
}
