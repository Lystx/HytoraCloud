package cloud.hytora.driver.http.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


public interface HttpAuthHandler {

	@Nullable
	HttpAuthUser getAuthUser(@Nonnull String token);

}
