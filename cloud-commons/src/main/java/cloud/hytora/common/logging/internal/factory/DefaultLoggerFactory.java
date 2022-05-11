package cloud.hytora.common.logging.internal.factory;

import cloud.hytora.common.logging.Logger;
import cloud.hytora.common.logging.LoggerFactory;
import cloud.hytora.common.logging.LogLevel;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;


public class DefaultLoggerFactory implements LoggerFactory {

	protected final Map<String, Logger> loggers = new ConcurrentHashMap<>();
	protected final Function<? super String, ? extends Logger> creator;
	protected LogLevel level = LogLevel.DEBUG;

	public DefaultLoggerFactory(@Nonnull Function<? super String, ? extends Logger> creator) {
		this.creator = creator;
	}

	@Nonnull
	@Override
	@CheckReturnValue
	public synchronized Logger forName(@Nullable String name) {
		return loggers.computeIfAbsent(name == null ? "anonymous" : name, unused -> creator.apply(name).setMinLevel(level));
	}

	@Override
	public void setDefaultLevel(@Nonnull LogLevel level) {
		this.level = level;
	}

}
