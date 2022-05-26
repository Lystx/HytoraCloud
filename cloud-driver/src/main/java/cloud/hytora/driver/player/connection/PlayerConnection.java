package cloud.hytora.driver.player.connection;

import cloud.hytora.driver.networking.protocol.ProtocolAddress;
import cloud.hytora.driver.networking.protocol.codec.buf.Bufferable;

import javax.annotation.Nonnull;

public interface PlayerConnection extends Bufferable {

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
