package cloud.hytora.http.api;



import cloud.hytora.http.HttpAddress;

import javax.annotation.Nonnull;


public interface HttpChannel {

	@Nonnull
	HttpAddress getServerAddress();

	@Nonnull
	HttpAddress getClientAddress();

	void close();

}
