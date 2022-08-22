package cloud.hytora.node.console.jline2.helper;

import cloud.hytora.node.console.jline2.completer.CandidateCompleter;
import cloud.hytora.node.console.jline2.completer.CandidateCompletionHandler;
import jline.Terminal;
import jline.TerminalFactory;
import jline.console.ConsoleReader;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Consumer;

/**
 * The task to catch commands at console input
 */
@AllArgsConstructor
public class TerminalTask implements Runnable {

    /**
     * The console reader
     */
    private final ConsoleReader reader;

    /**
     * The consumer when a new line is entered
     */
    @Getter
    private final Consumer<String> newLine;

    @Override
    public void run() {
        try {
            //TerminalFactory.registerFlavor(TerminalFactory.Flavor.WINDOWS, UnsupportedTerminal.class);
            Terminal terminal = TerminalFactory.get();
            terminal.setEchoEnabled(true);

            // set prompt and tab completion
            reader.setCompletionHandler(new CandidateCompletionHandler());
            reader.addCompleter(new CandidateCompleter());

            String line;
            while ((line = reader.readLine()) != null) {
                newLine.accept(line);
            }
        } catch (Exception e) {
            // mostly this error happens when the console is not exited properly
            // I only witnesses it while executing the cloud on linux and closing
            // the console forcefully
            System.err.println("Error while initializing JLine Terminal! (" + e.getMessage() + ")");
            e.printStackTrace();
        } finally {
            try {
                TerminalFactory.get().restore();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
