package cloud.hytora.driver.http.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;


public class HttpAuthRegistry {

	private final Map<String, HttpAuthHandler> handlers = new LinkedHashMap<>();

	@Nonnull
	public HttpAuthRegistry registerAuthMethodHandler(@Nonnull String type, @Nonnull HttpAuthHandler handler) {
		handlers.put(type, handler);
		return this;
	}

	@Nonnull
	public HttpAuthRegistry unregisterAuthMethodHandler(@Nonnull String type) {
		handlers.remove(type);
		return this;
	}

	@Nullable
	public HttpAuthHandler getAuthMethodHandler(@Nonnull String type) {
		return handlers.get(type);
	}

}
