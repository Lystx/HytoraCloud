package cloud.hytora.node.console;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.driver.console.Screen;
import cloud.hytora.driver.console.ScreenManager;
import cloud.hytora.driver.console.TabCompleter;
import cloud.hytora.node.NodeDriver;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

@Getter @Setter
public class NodeScreen implements Screen {

    private String name;
    private final List<String> allCachedLines;
    private final Collection<Consumer<? super String>> inputHandlers;
    private TabCompleter tabCompleter;

    public NodeScreen(String name) {
        this.name = name;
        this.allCachedLines = new ArrayList<>();
        this.inputHandlers = new ArrayList<>();
    }

    @Override
    public Screen registerTabCompleter(TabCompleter completer) {
        this.tabCompleter = completer;
        return this;
    }

    @Override
    public TabCompleter getCurrentTabCompleter() {
        return tabCompleter;
    }

    @Override
    public void writeLine(String line) {
        this.cacheLine(line);

        if (CloudDriver.getInstance().getProvider(ScreenManager.class).isScreenActive(this.name)) {
            CommandSender commandSender = CloudDriver.getInstance().getCommandSender();
            if (commandSender != null) {
                commandSender.forceMessage(line);
            }
        }
    }

    @Override
    public void cacheLine(String line) {
        this.allCachedLines.add(line);
    }

    @Override
    public void addInputHandler(@NotNull Consumer<? super String> handler) {
        this.inputHandlers.add(handler);
    }

    @Override
    public void clear() {
        NodeDriver.getInstance().getConsole().clearScreen();
    }


    @Override
    public void suggestInput(String input) {
        NodeDriver.getInstance().getConsole().setCommandInputValue(input);
    }

    @Override
    public Collection<String> getHistory() {
        return NodeDriver.getInstance().getConsole().getCommandHistory();
    }

    @Override
    public void setHistory(Collection<String> history) {
        NodeDriver.getInstance().getConsole().setCommandHistory((List<String>) history);
    }

    @Override
    public void clearCache() {
        this.allCachedLines.clear();
    }

    @Override
    public String readLineOrNull() {
        return NodeDriver.getInstance().getConsole().readLineOrNull();
    }

}
