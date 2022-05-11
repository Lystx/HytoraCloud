package cloud.hytora.common.logging;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;


public interface LoggerFactory {

	@Nonnull
	@CheckReturnValue
	Logger forName(@Nullable String name);

	void setDefaultLevel(@Nonnull LogLevel level);

}
