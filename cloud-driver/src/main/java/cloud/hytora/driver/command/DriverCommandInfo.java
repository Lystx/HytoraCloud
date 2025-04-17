package cloud.hytora.driver.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * A {@link DriverCommandInfo} is sent from Node-Instance to all running {@link cloud.hytora.driver.services.ICloudService}s
 * to check if certain ingame-commands are Cloud-based-commands so that they can be executed from the Node
 *
 * @see CommandScope#CONSOLE_AND_INGAME
 * @see CommandScope#INGAME
 * @see CommandScope#INGAME_HOSTED_ON_CLOUD_SIDE
 *
 * @since DEV-1.0
 * @version SNAPSHOT-1.0
 * @author Lystx
 */
@AllArgsConstructor @Getter @NoArgsConstructor
public class DriverCommandInfo {

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
