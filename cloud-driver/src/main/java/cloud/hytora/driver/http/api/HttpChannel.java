package cloud.hytora.driver.http.api;

import cloud.hytora.driver.networking.protocol.ProtocolAddress;

import javax.annotation.Nonnull;


public interface HttpChannel {

	@Nonnull
	ProtocolAddress getServerAddress();

	@Nonnull
	ProtocolAddress getClientAddress();

	void close();

}
