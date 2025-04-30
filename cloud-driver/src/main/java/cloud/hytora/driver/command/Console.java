package cloud.hytora.driver.command;

import cloud.hytora.common.logging.handler.LogEntry;
import cloud.hytora.common.progressbar.ProgressPrinter;
import cloud.hytora.driver.setup.Setup;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface Console extends ProgressPrinter {

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

	@Nonnull
	List<String> getAllWroteLines();


	void addInputHandler(@Nonnull Consumer<? super String> handler);

	@Nonnull
	Collection<Consumer<? super String>> getInputHandlers();

}
