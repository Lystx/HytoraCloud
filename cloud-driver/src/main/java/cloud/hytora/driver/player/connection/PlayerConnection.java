package cloud.hytora.driver.player.connection;

import cloud.hytora.driver.networking.protocol.ProtocolAddress;
import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;

import javax.annotation.Nonnull;

public interface PlayerConnection extends IBufferObject {

	@Nonnull
	String getProxyName();

	@Nonnull
	ProtocolAddress getAddress();

	@Nonnull
	ProtocolVersion getVersion();

	int getRawVersion();

	boolean isOnlineMode();

	boolean isLegacy();

}
