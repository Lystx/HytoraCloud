package cloud.hytora.node.console.handler;

import cloud.hytora.common.logging.handler.LogEntry;
import cloud.hytora.common.logging.handler.LogHandler;
import cloud.hytora.driver.console.Console;
import lombok.AllArgsConstructor;

import javax.annotation.Nonnull;

@AllArgsConstructor
public class ConsoleLogHandler implements LogHandler {

	private final Console console;

	@Override
	public void handle(@Nonnull LogEntry entry) {
		console.writeEntry(entry);
	}

}
