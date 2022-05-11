package cloud.hytora.common.logging.internal.factory;

import cloud.hytora.common.logging.Logger;
import cloud.hytora.common.logging.LoggerFactory;
import cloud.hytora.common.logging.LogLevel;
import cloud.hytora.common.logging.internal.WrappedSlf4jLogger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Slf4jLoggerFactory implements LoggerFactory {

	@Nonnull
	@Override
	public Logger forName(@Nullable String name) {
		org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(name == null ? "Logger" : name);
		return logger instanceof Logger ? (Logger) logger : new WrappedSlf4jLogger(logger);
	}

	@Override
	public void setDefaultLevel(@Nonnull LogLevel level) {
	}

}
