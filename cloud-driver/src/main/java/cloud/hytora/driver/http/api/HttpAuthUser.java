package cloud.hytora.driver.http.api;

import javax.annotation.Nonnull;


public interface HttpAuthUser {

	boolean hasPermission(@Nonnull String permission);

}
