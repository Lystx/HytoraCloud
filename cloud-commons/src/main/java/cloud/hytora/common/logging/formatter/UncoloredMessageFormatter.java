package cloud.hytora.common.logging.formatter;

import cloud.hytora.common.logging.handler.LogEntry;
import cloud.hytora.common.logging.handler.LogHandler;

import javax.annotation.Nonnull;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;


public final class UncoloredMessageFormatter {

	@Nonnull
	public static String format(@Nonnull LogEntry entry) {
		StringBuilder builder = new StringBuilder()
			.append("[")
			.append(LogHandler.TIME_FORMAT.format(Date.from(entry.getTimestamp())))
			.append(" ")
			.append(entry.getThreadName())
			.append("] ")
			.append(entry.getLevel().getName())
			.append(": ")
			.append(entry.getMessage());

		if (entry.getException() != null) {
			StringWriter writer = new StringWriter();
			entry.getException().printStackTrace(new PrintWriter(writer));
			builder.append(System.lineSeparator()).append(writer);
		}

		return builder.toString();
	}

	private UncoloredMessageFormatter() {}

}
