package cloud.hytora.node.console.jline2;

import cloud.hytora.common.VersionInfo;
import cloud.hytora.common.logging.LogLevel;
import cloud.hytora.common.logging.Logger;
import cloud.hytora.common.logging.formatter.ColoredMessageFormatter;
import cloud.hytora.common.logging.handler.LogEntry;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.console.Console;
import cloud.hytora.driver.console.screen.Screen;
import cloud.hytora.driver.console.screen.ScreenManager;
import cloud.hytora.node.console.ColorTranslator;
import jline.console.ConsoleReader;
import jline.console.history.History;
import lombok.Getter;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

@Getter
public class JLine2Console implements Console {


    private final String originalPrompt;
    private String prompt;
    private String screenName;
    private final Collection<Consumer<? super String>> inputHandlers = new ArrayList<>();

    @Getter
    private ConsoleReader reader;

    public JLine2Console(String prompt) throws Exception {
        System.setProperty("library.jansi.version", "HytoraCloud");
        try {
            AnsiConsole.systemInstall();
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

        // list jline console
        try {
            reader = new ConsoleReader();
            reader.setExpandEvents(false);
        } catch (IOException e) {
            System.err.println("Could not initialise console reader!");
            e.printStackTrace();
        }

        this.screenName = "Console";
        this.originalPrompt = prompt;
        this.prompt = prompt;
        this.resetPrompt();

    }


    @Override
    public void setCommandInputValue(@NotNull String commandInputValue) {
        this.reader.getCursorBuffer().write(commandInputValue);
    }

    @Override
    public void resetPrompt() {
        this.prompt = originalPrompt;
        this.updatePrompt();
    }

    @Override
    public void setCommandHistory(List<String> history) {
        this.reader.getHistory().clear();

        if (history != null) {
            for (String s : history) {
                this.reader.getHistory().add(s);
            }
        }
    }

    @Override
    public Collection<String> getCommandHistory() {
        List<String> result = new ArrayList<>();
        for (ListIterator<jline.console.history.History.Entry> it = this.reader.getHistory().entries(); it.hasNext(); ) {
            History.Entry entry = it.next();
            result.add(String.valueOf(entry.value()));
        }

        return result;
    }

    @Override
    public void clearScreen() {
        try {
            this.reader.clearScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws Exception {
        this.reader.flush();
        this.reader.shutdown();

        AnsiConsole.systemUninstall();
    }

    @Override
    public String readLineOrNull() {
        try {
            return this.reader.readLine();
        } catch (IOException e) {
            return null;
        }
    }

    @NotNull
    @Override
    public Console writeLine(@NotNull String text) {
        if (!text.endsWith(System.lineSeparator()) && !text.startsWith("\r")) {
            text += System.lineSeparator();
        }
        ScreenManager sm = CloudDriver.getInstance().getProviderRegistry().getUnchecked(ScreenManager.class);
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
    public Console writeEntry(@NotNull LogEntry entry) {
        String formatted = ColoredMessageFormatter.format(entry);

        if (!formatted.endsWith(System.lineSeparator()) && !entry.getMessage().startsWith("\r")) {
            formatted += System.lineSeparator();
        }
        if (entry.getMessage().startsWith("\r")) {
            formatted = "\r" + formatted;
        }
        ScreenManager sm = CloudDriver.getInstance().getProviderRegistry().getUnchecked(ScreenManager.class);
        Screen console = sm.getScreenByNameOrNull("Console");
        if (console != null) {
            console.cacheLine(entry.getMessage());
        }
        if (sm.isScreenActive("console")) {
            forceWrite(formatted);
        }
        return this;
    }

    @Override
    public Console forceWrite(String text) {
        if (!text.endsWith(System.lineSeparator()) && !text.startsWith("\r")) {
            text += System.lineSeparator();
        }
        if (text.startsWith("\r")) {
            print(text);
            return this;
        }
        print(Ansi.ansi().eraseLine(Ansi.Erase.ALL).toString() + '\r' + text + Ansi.ansi().reset().toString());
        return this;
    }


    @Override
    public Console writePlain(String text) {
        print(text);
        return this;
    }

    private void print(@Nonnull String text) {
        try {
            this.reader.print(text);
            this.reader.drawLine();
            this.reader.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void setPrompt(@Nonnull String prompt) {
        this.prompt = prompt;
        updatePrompt();
    }


    private void updatePrompt() {
        prompt = ColorTranslator
                .translate(prompt)
                .replace("%node%", (CloudDriver.getInstance() == null || CloudDriver.getInstance().thisSidesClusterParticipant() == null) ? "Node" : CloudDriver.getInstance().thisSidesClusterParticipant().getName())
                .replace("%screen%", screenName);
        this.reader.setPrompt(prompt);
    }

    @Override
    public void setScreenName(@Nonnull String screenName) {
        this.screenName = screenName;
        this.resetPrompt();
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

    @Override
    public void printHeader() {
        Logger logger = Logger.constantInstance();

        logger.info("HytoraCloud in its current State of Development is presented to you by Lystx and Contributors...");

        logger.log(LogLevel.NULL, "§b                       _                    §f___ _                 _ ");
        logger.log(LogLevel.NULL, "§b           /\\  /\\_   _| |_ ___  _ __ __ _  §f/ __\\ | ___  _   _  __| |");
        logger.log(LogLevel.NULL, "§b          / /_/ / | | | __/ _ \\| '__/ _` |§f/ /  | |/ _ \\| | | |/ _` |");
        logger.log(LogLevel.NULL, "§b         / __  /| |_| | || (_) | | | (_| §f/ /___| | (_) | |_| | (_| |");
        logger.log(LogLevel.NULL, "§b         \\/ /_/  \\__, |\\__\\___/|_|  §f\\__,_\\____/|_|\\___/ \\__,_|\\__,_|");
        logger.log(LogLevel.NULL, "§b                 |___/                  §f                            ");
        logger.log(LogLevel.NULL, "            §8x §eSmurf V1 §8- §fWhere opportunity connects §8x");
        logger.log(LogLevel.NULL, " ");
        logger.log(LogLevel.NULL, "           §8=>    §fCloud Version " + VersionInfo.getCurrentVersion().toString());
        logger.log(LogLevel.NULL, "           §8=>    §fJava Version " + System.getProperty("java.version"));
        logger.log(LogLevel.NULL, "           §8=>    §fDiscord " + "https://discord.com/invite/WRYH33X7Fu");
        logger.log(LogLevel.NULL, "");
        logger.info("§8");
    }

}
