package cloud.hytora.driver.console;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.function.Consumer;

public interface Screen {

    void writeLine(String line);

    void cacheLine(String line);

    Screen registerTabCompleter(TabCompleter completer);

    void suggestInput(String input);

    Collection<String> getHistory();

    void setHistory(Collection<String> history);

    TabCompleter getCurrentTabCompleter();

    void addInputHandler(@Nonnull Consumer<? super String> handler);

    String getName();

    void setName(String name);

    void clear();

    void clearCache();

    @Nonnull
    Collection<Consumer<? super String>> getInputHandlers();

    Collection<String> getAllCachedLines();

    String readLineOrNull();
}
