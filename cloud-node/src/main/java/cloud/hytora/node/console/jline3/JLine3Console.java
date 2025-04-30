package cloud.hytora.node.console.jline3;

import cloud.hytora.common.logging.ConsoleColor;
import cloud.hytora.common.logging.Logger;
import cloud.hytora.common.logging.formatter.ColoredMessageFormatter;
import cloud.hytora.common.logging.handler.LogEntry;
import cloud.hytora.common.misc.ReflectionUtils;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.console.Screen;
import cloud.hytora.driver.console.ScreenManager;
import cloud.hytora.node.console.ColorTranslator;
import cloud.hytora.driver.command.Console;
import cloud.hytora.node.console.ConsoleReadThread;
import lombok.Getter;
import lombok.Setter;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.jetbrains.annotations.NotNull;
import org.jline.reader.Completer;
import org.jline.reader.History;
import org.jline.reader.LineReader;
import org.jline.reader.LineReader.Option;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;

@Getter
@Setter
public class JLine3Console implements Console {

    private final ConsoleReadThread consoleReadThread = new ConsoleReadThread(this);
    private final Collection<Consumer<? super String>> inputHandlers = new ArrayList<>();
    private final List<String> allWroteLines = new ArrayList<>();

    private final Terminal terminal;
    private final LineReader lineReader;

    private final String promptTemplate;
    private String prompt = null;
    private String screenName = "Console";

    public JLine3Console(String promptTemplate) throws Exception {
        System.setProperty("library.jansi.version", "HytoraCloud");
        this.promptTemplate = promptTemplate;
        try {
            AnsiConsole.systemInstall();
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

        this.terminal = TerminalBuilder.builder().system(true).encoding(StandardCharsets.UTF_8).build();

        this.lineReader = new InternalLineReaderBuilder(terminal).completer(new JLine3Completer()).option(Option.DISABLE_EVENT_EXPANSION, true).variable(LineReader.BELL_STYLE, "off").build();

        this.resetPrompt();
        consoleReadThread.start();
    }

    @Override
    public void setCommandHistory(List<String> history) {
        try {
            this.lineReader.getHistory().purge();
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        if (history != null) {
            for (String s : history) {
                this.lineReader.getHistory().add(s);
            }
        }
    }
    @Override
    public void setCommandInputValue(@Nonnull String commandInputValue) {
        lineReader.getBuffer().write(commandInputValue);
    }

    @Override
    public @NotNull List<String> getCommandHistory() {
        List<String> result = new ArrayList<>();
        for (History.Entry entry : this.lineReader.getHistory()) {
            result.add(entry.line());
        }

        return result;
    }

    @Override
    public String readLineOrNull() {
        return consoleReadThread.readLineOrNull();
    }

    @Override
    public Console writeEntry(@NotNull LogEntry entry) {
        String formatted = ColoredMessageFormatter.format(entry);

        if (!formatted.endsWith(System.lineSeparator()) && !entry.getMessage().startsWith("\r")) {
            formatted += System.lineSeparator();
        }
        ScreenManager sm = CloudDriver.getInstance().getProvider(ScreenManager.class);
        Screen console = sm.getScreenByNameOrNull("console");
        if (console != null) {
            console.cacheLine(entry.getMessage());
        }
        if (sm.isScreenActive("console")) {
            forceWrite(formatted);
        }
        return this;
    }

    @Nonnull
    @Override
    public Console writeLine(@Nonnull String text) {
        if (!text.endsWith(System.lineSeparator()) && !text.startsWith("\r")) {
            text += System.lineSeparator();
        }
        ScreenManager sm = CloudDriver.getInstance().getProvider(ScreenManager.class);
        Screen console = sm.getScreenByNameOrNull("console");
        if (console != null) {
            console.cacheLine(text);
        }
        if (sm.isScreenActive("console")) {
            forceWrite(text);
        }
        return this;
    }

    @Override
    public Console forceWrite(String text) {
        if (!text.endsWith(System.lineSeparator()) && !text.startsWith("\r")) {
            text += System.lineSeparator();
        }
        if (text.startsWith("\r")) {
            print0(text);
            return this;
        }
        print0(Ansi.ansi().eraseLine(Ansi.Erase.ALL).toString() + '\r' + text + Ansi.ansi().reset().toString());
        return this;
    }

    @Override
    public void print(String text) {
        text = ConsoleColor.toColoredString('§', text);
        print0(Ansi.ansi().eraseLine(Ansi.Erase.ALL).toString() + text + Ansi.ansi().reset().toString());
    }

    @Override
    public void resetPrompt() {
        prompt = promptTemplate;
        updatePrompt();
    }

    @Override
    public void clearScreen() {
        ReflectionUtils.clearConsole();
    }

    @Override
    public void close() throws Exception {
        consoleReadThread.interrupt();

        terminal.flush();
        terminal.close();

        AnsiConsole.systemUninstall();
    }

    @Nonnull
    @Override
    public String getPrompt() {
        return prompt;
    }

    @Override
    public void setPrompt(@Nonnull String prompt) {
        this.prompt = prompt;
        updatePrompt();
    }

    @Override
    public void setScreenName(@Nonnull String screenName) {
        this.screenName = screenName;
        resetPrompt();
    }

    @Override
    public void addInputHandler(@Nonnull Consumer<? super String> handler) {
        Logger.constantInstance().debug("Added InputHandler {} to console!", handler);
        inputHandlers.add(handler);
    }

    @Nonnull
    @Override
    public Collection<Consumer<? super String>> getInputHandlers() {
        return new ArrayList<>(inputHandlers);
    }

    private void updatePrompt() {
        prompt = ColorTranslator.translate(prompt).replace("%screen%", screenName);
        ((LineReaderImpl) lineReader).setPrompt(prompt);
    }

    private void print0(@Nonnull String text) {
        lineReader.getTerminal().puts(InfoCmp.Capability.carriage_return);
        lineReader.getTerminal().writer().print(text);
        lineReader.getTerminal().writer().flush();


        if (!lineReader.isReading()) {
            return;
        }

        lineReader.callWidget(LineReader.REDRAW_LINE);
        lineReader.callWidget(LineReader.REDISPLAY);
    }

    @Override
    public void flush(String progress) {

    }


    @Nonnull
    public LineReader getLineReader() {
        return lineReader;
    }

    private static class InternalLineReader extends LineReaderImpl {

        private InternalLineReader(Terminal terminal, String appName, Map<String, Object> variables) {
            super(terminal, appName, variables);
        }

        @Override
        protected boolean historySearchBackward() {
            if (history.previous()) {
                setBuffer(history.current());
                return true;
            } else {
                return false;
            }
        }

        @Override
        protected boolean historySearchForward() {
            if (history.next()) {
                setBuffer(history.current());
                return true;
            } else {
                return false;
            }
        }

        @Override
        protected boolean upLineOrSearch() {
            return historySearchBackward();
        }

        @Override
        protected boolean downLineOrSearch() {
            return historySearchForward();
        }
    }

    private static final class InternalLineReaderBuilder {

        private final Terminal terminal;
        private final Map<String, Object> variables = new HashMap<>();
        private final Map<Option, Boolean> options = new HashMap<>();
        private Completer completer;

        private InternalLineReaderBuilder(@Nonnull Terminal terminal) {
            this.terminal = terminal;
        }

        @Nonnull
        public InternalLineReaderBuilder variable(@Nonnull String name, @Nonnull Object value) {
            variables.put(name, value);
            return this;
        }

        @Nonnull
        public InternalLineReaderBuilder option(@Nonnull Option option, boolean value) {
            options.put(option, value);
            return this;
        }

        @Nonnull
        public InternalLineReaderBuilder completer(@Nonnull Completer completer) {
            this.completer = completer;
            return this;
        }

        @Nonnull
        public InternalLineReader build() {
            InternalLineReader reader = new InternalLineReader(terminal, "CloudConsole", variables);
            if (completer != null) {
                reader.setCompleter(completer);
            }

            for (Entry<Option, Boolean> entry : options.entrySet()) {
                reader.option(entry.getKey(), entry.getValue());
            }

            return reader;
        }
    }

}
