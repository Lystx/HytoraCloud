package cloud.hytora.common.logging.handler;

import cloud.hytora.common.logging.LogLevel;
import cloud.hytora.common.collection.NamedThreadFactory;

import javax.annotation.Nonnull;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class HandledAsyncLogger extends HandledLogger {

	protected final Executor executor = Executors.newSingleThreadExecutor(new NamedThreadFactory("AsyncLogTask"));

	public HandledAsyncLogger(@Nonnull LogLevel initialLevel) {
		super(initialLevel);
	}

	@Override
	protected void log0(@Nonnull LogEntry entry) {
		executor.execute(() -> logNow(entry));
	}

}
