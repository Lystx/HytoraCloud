package cloud.hytora.common.logging.handler;

import cloud.hytora.common.logging.LogLevel;

import javax.annotation.Nonnull;


public class HandledSyncLogger extends HandledLogger {

	public HandledSyncLogger(@Nonnull LogLevel initialLevel) {
		super(initialLevel);
	}

	@Override
	protected void log0(@Nonnull LogEntry entry) {
		logNow(entry);
	}
}
