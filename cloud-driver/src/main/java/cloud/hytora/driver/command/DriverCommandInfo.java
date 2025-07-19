package cloud.hytora.driver.command;

import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.types.BufferState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * A {@link DriverCommandInfo} is sent from Node-Instance to all running {@link CloudService}s
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
public class DriverCommandInfo implements IBufferObject {

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

	@Override
	public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
		switch (state) {
			case READ:
				this.path = buf.readOptionalString();
				this.permission = buf.readOptionalString();
				this.scope = buf.readEnum(CommandScope.class);
				break;

			case WRITE:
				buf.writeOptionalString(this.path);
				buf.writeOptionalString(this.permission);
				buf.writeEnum(this.scope);

				break;
		}
	}
}
