package cloud.hytora.node.console;

import cloud.hytora.common.logging.Logger;
import cloud.hytora.common.misc.ReflectionUtils;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.driver.console.Screen;
import cloud.hytora.driver.console.ScreenManager;
import cloud.hytora.driver.console.TabCompleter;
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
    public void registerTabCompleter(TabCompleter completer) {
        this.tabCompleter = completer;
    }

    @Override
    public TabCompleter getCurrentTabCompleter() {
        return tabCompleter;
    }

    @Override
    public void writeLine(String line) {
        this.cacheLine(line);

        if (CloudDriver.getInstance().getProviderRegistry().getUnchecked(ScreenManager.class).isScreenActive(this.name)) {
            CommandSender commandSender = CloudDriver.getInstance().getCommandSender();
            if (commandSender != null) {
                commandSender.sendMessage(line);
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
        ReflectionUtils.clearConsole();
    }

    @Override
    public void clearCache() {
        this.allCachedLines.clear();
    }

}
