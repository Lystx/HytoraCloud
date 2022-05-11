package cloud.hytora.driver.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor @Getter @NoArgsConstructor
public class CommandObject {

	/**
	 * The path of this command
	 */
	private String path;

	/**
	 * The permission to access it
	 */
	private String permission;

}
