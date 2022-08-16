package cloud.hytora.common.logging.formatter;

import cloud.hytora.common.logging.ConsoleColor;
import cloud.hytora.common.logging.LogLevel;
import cloud.hytora.common.logging.handler.LogEntry;
import cloud.hytora.common.logging.handler.LogHandler;

import javax.annotation.Nonnull;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;


public final class ColoredMessageFormatter {

	@Nonnull
	public static String format(@Nonnull LogEntry entry) {
		if (entry.getLevel() == LogLevel.NULL) {
			return ConsoleColor.toColoredString('§', entry.getMessage());
		}
		StringBuilder builder = new StringBuilder()
			.append(ConsoleColor.DARK_GRAY)
			.append("[")
			.append(ConsoleColor.WHITE)
			.append(LogHandler.TIME_FORMAT.format(Date.from(entry.getTimestamp())))
			.append(" ");

		String name = entry.getThreadName();
		if (name.length() > 18) name = name.substring(name.length() - 18);
		SpacePadder.padLeft(builder, name, 18);

		builder.append(ConsoleColor.DARK_GRAY)
			.append("] ")
			.append(entry.getLevel().isHighlighted() ? ConsoleColor.CYAN : ConsoleColor.DEFAULT);

		SpacePadder.padRight(builder, entry.getLevel().getName() + ConsoleColor.DARK_GRAY + ":", 10 + ConsoleColor.DARK_GRAY.toString().length());
		builder.append(entry.getLevel().isHighlighted() ? (entry.getLevel().getHighlightColor() != null ? entry.getLevel().getHighlightColor() : ConsoleColor.YELLOW) : ConsoleColor.DEFAULT)
			.append(ConsoleColor.toColoredString('§', entry.getMessage()));

		if (entry.getException() != null) {
			StringWriter writer = new StringWriter();
			entry.getException().printStackTrace(new PrintWriter(writer));
			builder.append(System.lineSeparator()).append(writer);
		}

		return builder.toString();
	}

	private ColoredMessageFormatter() {}
}
