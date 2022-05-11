package cloud.hytora.driver.command.annotation.data;

import cloud.hytora.driver.command.completer.CommandCompleter;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor @Getter
public class RegisteredCommandArgument {

	private final String name;
	private final Class<?> objectClass;
	private final Class<? extends CommandCompleter> completerClass;
	private final int words;
	private final boolean raw;
	private final boolean optional;

}
