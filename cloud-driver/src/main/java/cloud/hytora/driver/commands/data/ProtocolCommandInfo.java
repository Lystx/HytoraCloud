package cloud.hytora.driver.commands.data;

import cloud.hytora.driver.commands.data.enums.CommandScope;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor @Getter @NoArgsConstructor
public class ProtocolCommandInfo {

	/**
	 * The path of this command
	 */
	private String path;

	/**
	 * The permission to access it
	 */
	private String permission;

	/**
	 * The scope to execute it
	 */
	private CommandScope scope;
}
