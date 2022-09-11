package cloud.hytora.http.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


public interface HttpAuthHandler {

	@Nullable
	HttpAuthUser getAuthUser(@Nonnull HttpContext context, @Nonnull String token);

}
