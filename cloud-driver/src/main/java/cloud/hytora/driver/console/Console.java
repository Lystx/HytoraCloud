package cloud.hytora.driver.console;

import cloud.hytora.common.logging.handler.LogEntry;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public interface Console {

	void setCommandInputValue(@Nonnull String commandInputValue);

	void resetPrompt();

	void setCommandHistory(List<String> history);

	Collection<String> getCommandHistory();

	void clearScreen();

	void close() throws Exception;

	@Nonnull
	String getPrompt();

	void setPrompt(@Nonnull String prompt);

	@Nonnull
	String getScreenName();

	void setScreenName(@Nonnull String screen);

	String readLineOrNull();

	@Nonnull
	Console writeLine(@Nonnull String text);

	Console writeEntry(@Nonnull LogEntry entry);

	Console forceWrite(String text);

	Console writePlain(String text);

	void addInputHandler(@Nonnull Consumer<? super String> handler);

	@Nonnull
	Collection<Consumer<? super String>> getInputHandlers();

    void printHeader();
}
