package cloud.hytora.driver.http.api;

import javax.annotation.Nonnull;
import java.util.Collection;


public interface WebSocketChannel {

	@Nonnull
    HttpChannel getChannel();

	@Nonnull
    HttpServer getServer();

	void close();

	void close(int statusCode, @Nonnull String closeReason);

	void sendFrame(@Nonnull WebSocketFrameType type, @Nonnull byte[] data);

	void sendFrame(@Nonnull WebSocketFrameType type, @Nonnull String text);

	@Nonnull
	Collection<WebSocketListener> getListeners();

	void addListener(@Nonnull WebSocketListener listener);

	default void addListeners(@Nonnull WebSocketListener... listeners) {
		for (WebSocketListener listener : listeners)
			addListener(listener);
	}

	void clearListeners();

}
