package cloud.hytora.driver.http.api;

import cloud.hytora.driver.http.api.HttpContext;
import cloud.hytora.driver.http.api.HttpMethod;

import javax.annotation.Nonnull;


public interface RegisteredHandler {

	void execute(@Nonnull HttpContext context) throws Exception;

	/**
	 * The path of the handler.
	 * The path must start with '/' and must not end with '/'.
	 *
	 * @return the path of the handler
	 */
	@Nonnull
	String getPath();

	@Nonnull
	String getPermission();

	@Nonnull
	HttpMethod[] getMethods();

	@Nonnull
	Object getHolder();

}
