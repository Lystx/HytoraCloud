package cloud.hytora.driver.entity.player.connection;

import cloud.hytora.driver.networking.protocol.ProtocolAddress;
import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public interface PlayerConnection extends IBufferObject {

	@Nonnull
	UUID getConnectionId();

	@Nullable
	String getConnectionName();

	void setConnectionName(String name);

	@Nonnull
	String getProxyName();

	@Nonnull
	ProtocolAddress getAddress();

	@Nonnull
	ProtocolVersion getVersion();

	int getRawVersion();

	boolean isOnlineMode();

	boolean isLegacy();


	void disconnect(String reason);

}
