package cloud.hytora.node.console.jline3;

import lombok.Getter;
import org.jline.reader.UserInterruptException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ConsoleReadThread extends Thread {

	private final JLine3Console console;

	@Getter
	private String lastLine;

	public ConsoleReadThread(@Nonnull JLine3Console console) {
		super("ConsoleThread");
		this.console = console;
	}

	@Override
	public void run() {
		String line;
		while (!Thread.interrupted() && (line = readLineOrNull()) != null) {
			lastLine = line;
			for (Consumer<? super String> handler : new ArrayList<>(console.getInputHandlers())) {
				handler.accept(line);
			}
		}
	}

	@Nullable
	public String readLineOrNull() {
		try {
			return console.getLineReader().readLine(console.getPrompt());
		} catch (Exception ex) {
			return null;
		}
	}

}
