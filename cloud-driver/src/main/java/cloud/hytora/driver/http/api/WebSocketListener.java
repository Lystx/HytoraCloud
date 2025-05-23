package cloud.hytora.driver.http.api;

import javax.annotation.Nonnull;


public interface WebSocketListener {

	void handle(@Nonnull WebSocketChannel channel, @Nonnull WebSocketFrameType type, @Nonnull byte[] data);

}
