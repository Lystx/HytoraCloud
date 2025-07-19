package cloud.hytora.driver.networking.packets.auth;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import cloud.hytora.driver.networking.protocol.types.BufferState;
import cloud.hytora.driver.entity.services.utils.RemoteIdentity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PacketAuthentication extends AbstractPacket {

	private AuthenticationPayload payload;
	private RemoteIdentity identity;


	@Override
	public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
		switch (state) {
			case WRITE:
				buf.writeEnum(payload);
				buf.writeObject(identity);
				break;
			case READ:
				this.payload = buf.readEnum(AuthenticationPayload.class);
				this.identity = buf.readObject(RemoteIdentity.class);
				break;
			default:
		}
	}

	public enum AuthenticationPayload {
		NODE,
		SERVICE
	}

}
