package cloud.hytora.driver.http.api;


import cloud.hytora.document.Document;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

/**

 *
 * @see HttpResponse
 * @see HttpRequest
 */
public interface HttpMessage<M extends HttpMessage<?>> {

	@Nonnull
	HttpContext getContext();

	@Nullable
	String getHeader(@Nonnull String name);

	int getHeaderInt(@Nonnull String name);

	boolean getHeaderBoolean(@Nonnull String name);

	boolean hasHeader(@Nonnull String name);

	@Nonnull
	M setHeader(@Nonnull String name, @Nonnull String value);

	@Nonnull
	M removeHeader(@Nonnull String name);

	@Nonnull
	M clearHeaders();

	@Nonnull
	Map<String, String> getHeaders();

	@Nonnull
	HttpVersion getVersion();

	@Nonnull
	M setVersion(@Nonnull HttpVersion version);

	@Nonnull
	byte[] getBody();

	@Nonnull
	String getBodyString();

	@Nonnull
	M setBody(@Nonnull byte[] data);

	@Nonnull
	M setBody(@Nonnull String text);

	@Nonnull
	M setBody(@Nonnull Document document);

	@Nonnull
	M setBody(@Nonnull Collection<Document> array);

	@Nonnull
	M setBody(@Nonnull Document[] array);
}
