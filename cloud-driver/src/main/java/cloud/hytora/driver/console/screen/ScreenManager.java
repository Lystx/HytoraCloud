package cloud.hytora.driver.console.screen;

import cloud.hytora.common.task.IPromise;

import javax.annotation.Nullable;
import java.util.Collection;

public interface ScreenManager {

    IPromise<Screen> getScreenByName(String name);

    Screen getScreenByNameOrNull(String name);

    void joinScreen(Screen screen);

    void leaveCurrentScreen();

    boolean isCurrentlyInScreen();

    @Nullable Screen getCurrentScreen();

    boolean isCurrentScreenAllowCommandManager();

    Screen registerScreen(String name, boolean enableCommandManager);

    boolean isScreenActive(String name);

    void unregisterScreen(String name);

    Collection<Screen> getRegisteredScreens();
}
