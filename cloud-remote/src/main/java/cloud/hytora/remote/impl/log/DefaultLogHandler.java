package cloud.hytora.remote.impl.log;

import cloud.hytora.common.logging.LogLevel;
import cloud.hytora.common.logging.formatter.UncoloredMessageFormatter;
import cloud.hytora.common.logging.handler.LogEntry;
import cloud.hytora.common.logging.handler.LogHandler;

import javax.annotation.Nonnull;
import java.io.PrintStream;


public class DefaultLogHandler implements LogHandler {

	private final PrintStream out = System.out, err = System.err;

	@Override
	public void handle(@Nonnull LogEntry entry) {
		PrintStream stream = entry.getLevel() == LogLevel.ERROR ? err : out;
		stream.println(UncoloredMessageFormatter.format(entry));
	}

}
