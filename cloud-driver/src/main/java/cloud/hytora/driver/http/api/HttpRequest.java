package cloud.hytora.driver.http.api;

import javax.annotation.Nonnull;
import java.net.URI;
import java.util.List;
import java.util.Map;


public interface HttpRequest extends HttpMessage<HttpRequest> {

	@Nonnull
	HttpMethod getMethod();

	@Nonnull
	URI getUri();

	@Nonnull
	String getPath();

	@Nonnull
	Map<String, List<String>> getQueryParameters();

	@Nonnull
	Map<String, String> getPathParameters();

}
