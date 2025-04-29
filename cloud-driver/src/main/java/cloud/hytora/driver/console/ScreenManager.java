package cloud.hytora.driver.console;

import cloud.hytora.common.task.Task;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Consumer;

public interface ScreenManager {

    Task<Screen> getScreenByName(String name);

    Screen getScreenByNameOrNull(String name);

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
