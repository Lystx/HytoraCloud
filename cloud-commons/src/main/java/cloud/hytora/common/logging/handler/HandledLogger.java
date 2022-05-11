package cloud.hytora.common.logging.handler;

import cloud.hytora.common.logging.ConsoleColor;
import cloud.hytora.common.logging.Logger;
import cloud.hytora.common.logging.LogLevel;
import cloud.hytora.common.misc.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;


public abstract class HandledLogger extends Logger {

	protected final Collection<LogHandler> handlers = new CopyOnWriteArrayList<>();
	protected LogLevel level;
	protected boolean translateColors;

	public HandledLogger(@Nonnull LogLevel initialLevel) {
		this.level = initialLevel;
	}

	@Override
	public void log(@Nonnull LogLevel level, @Nullable String message, @Nonnull Object... args) {
		if (!level.isEnabled(this.level)) return;
		if (translateColors && message != null) {
			message = ConsoleColor.toColoredString('§', message);
			message = ConsoleColor.toColoredString('&', message);
			translateColors = false;
		}
		Throwable exception = null;
		for (Object arg : args) {
			if (arg instanceof Throwable)
				exception = (Throwable) arg;
		}
		log0(new LogEntry(Instant.now(), Thread.currentThread().getName(), StringUtils.formatMessage(message, args), level, exception));
	}


	@Override
	public Logger translateColors() {
		translateColors = true;
		return this;
	}

	public void log(@Nonnull LogEntry entry) {
		if (!entry.getLevel().isEnabled(this.level)) return;
		log0(entry);
	}

	protected abstract void log0(@Nonnull LogEntry entry);

	protected void logNow(@Nonnull LogEntry entry) {
		for (LogHandler handler : handlers) {
			try {
				handler.handle(entry);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	@Nonnull
	public HandledLogger addHandler(@Nonnull LogHandler... handler) {
		handlers.addAll(Arrays.asList(handler));
		return this;
	}

	@Nonnull
	@Override
	public LogLevel getMinLevel() {
		return level;
	}

	@Nonnull
	@Override
	public Logger setMinLevel(@Nonnull LogLevel level) {
		this.level = level;
		return this;
	}

}
