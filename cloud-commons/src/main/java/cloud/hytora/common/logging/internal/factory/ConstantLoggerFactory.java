package cloud.hytora.common.logging.internal.factory;

import cloud.hytora.common.logging.Logger;
import cloud.hytora.common.logging.LoggerFactory;
import cloud.hytora.common.logging.LogLevel;
import lombok.AllArgsConstructor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


@AllArgsConstructor
public class ConstantLoggerFactory implements LoggerFactory {

	protected final Logger logger;

	@Nonnull
	@Override
	public Logger forName(@Nullable String name) {
		return logger;
	}

	@Override
	public void setDefaultLevel(@Nonnull LogLevel level) {
		logger.setMinLevel(level);
	}

}
