package cloud.hytora.driver.http.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


public interface HttpContext {

	@Nonnull
    WebSocketChannel upgrade();

	@Nullable
	WebSocketChannel getWebSocketChannel();

	@Nonnull
	HttpChannel getChannel();

	@Nonnull
	HttpServer getServer();

	@Nonnull
	HttpRequest getRequest();

	@Nonnull
	HttpResponse getResponse();

	@Nonnull
	HttpContext cancelNext(boolean cancel);

	boolean isCancelNext();

	@Nonnull
	HttpContext closeAfter(boolean close);

	boolean isCloseAfter();

	@Nonnull
	HttpContext cancelSendResponse(boolean cancel);

	boolean isCancelSendResponse();

}
