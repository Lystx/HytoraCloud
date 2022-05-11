package cloud.hytora.common.logging.internal;

import cloud.hytora.common.logging.ConsoleColor;
import cloud.hytora.common.logging.Logger;
import cloud.hytora.common.logging.LogLevel;
import cloud.hytora.common.misc.StringUtils;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.PrintStream;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class FallbackLogger extends Logger {

	protected final PrintStream stream = System.err;
	protected final String name;

	protected boolean translateColors;

	protected LogLevel level = LogLevel.INFO;

	public FallbackLogger(@Nullable String name) {
		this.name = name;
	}

	@Override
	public void log(@Nonnull LogLevel level, @Nullable String message, @Nonnull Object... args) {
		if (!isLevelEnabled(level)) return;
		if (translateColors && message != null) {
			message = ConsoleColor.toColoredString('§', message);
			message = ConsoleColor.toColoredString('&', message);
			translateColors = false;
		}
		stream.println(getLogMessage(level, StringUtils.formatMessage(message, args), name));
		for (Object arg : args) {
			if (!(arg instanceof Throwable)) continue;
			((Throwable)arg).printStackTrace(stream);
		}
	}

	@Override
	public Logger translateColors() {
		translateColors = true;
		return this;
	}

	@Nonnull
	@Override
	public FallbackLogger setMinLevel(@Nonnull LogLevel level) {
		this.level = level;
		return this;
	}

	@Nonnull
	@Override
	public LogLevel getMinLevel() {
		return level;
	}

	@Nonnull
	@CheckReturnValue
	public static String getLogMessage(@Nonnull LogLevel level, @Nonnull String message, @Nullable String name) {
		Thread thread = Thread.currentThread();
		String threadName = thread.getName();
		String time = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
		return name == null ?
				String.format("[%s: %s/%s]: %s", time, threadName, level.getName(), message) :
				String.format("[%s: %s/%s] %s: %s", time, threadName, level.getName(), name, message);
	}

}
