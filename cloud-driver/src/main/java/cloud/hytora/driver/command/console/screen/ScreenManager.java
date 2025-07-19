package cloud.hytora.driver.command.console.screen;

import cloud.hytora.common.task.Task;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Consumer;

public interface ScreenManager {

    Task<Screen> getScreen(String name);

    Screen getCachedScreen(String name);

    void joinScreen(Screen screen);

    void update(String name, Consumer<Screen> handler);

    void leaveCurrentScreen();

    boolean isCurrentlyInScreen();

    @Nullable Screen getCurrentScreen();

    boolean isCurrentScreenAllowCommandManager();

    Screen registerScreen(String name, boolean enableCommandManager);

    boolean isScreenActive(String name);

    void unregisterScreen(String name);

    Collection<Screen> getRegisteredScreens();
}
