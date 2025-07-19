package cloud.hytora.driver.common.http.api;

import javax.annotation.Nonnull;


public interface HttpAuthUser {

	boolean hasPermission(@Nonnull String permission);

}
