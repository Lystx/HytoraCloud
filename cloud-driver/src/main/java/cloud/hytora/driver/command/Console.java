package cloud.hytora.driver.command;

import cloud.hytora.driver.setup.Setup;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface Console {

	boolean isPrintingEnabled();

	void setPrintingEnabled(boolean enabled);

	void setCommandInputValue(@Nonnull String commandInputValue);

	void resetPrompt();

	void removePrompt();

	void emptyPrompt();

	void clearScreen();

	void close() throws Exception;

	@Nonnull
	String getPrompt();

	void setPrompt(@Nonnull String prompt);

	@Nonnull
	String getScreenName();

	void setScreenName(@Nonnull String screen);

	@Nonnull
	CompletableFuture<String> readLine();

	String readLineOrNull();

	@Nonnull
	Console writeRaw(@Nonnull String rawText);

	@Nonnull
	Console forceWrite(@Nonnull String text);

	@Nonnull
	Console forceWriteLine(@Nonnull String text);

	@Nonnull
	Console write(@Nonnull String text);

	@Nonnull
	Console writeLine(@Nonnull String text);

	@Nonnull
	List<String> getAllWroteLines();

	void setLineCaching(boolean active);

	void addInputHandler(@Nonnull Consumer<? super String> handler);

	@Nonnull
	Collection<Consumer<? super String>> getInputHandlers();

	Setup<?> getCurrentSetup();

    void setCurrentSetup(Setup<?> tSetupExecutor);
}
