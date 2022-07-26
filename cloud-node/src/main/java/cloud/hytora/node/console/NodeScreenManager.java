package cloud.hytora.node.console;

import cloud.hytora.common.function.ExceptionallyBiConsumer;
import cloud.hytora.common.logging.Logger;
import cloud.hytora.common.misc.ReflectionUtils;
import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.driver.console.Screen;
import cloud.hytora.driver.console.ScreenManager;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class NodeScreenManager implements ScreenManager {

    private final Map<String, Screen> allCachedScreens;
    private final Map<String, Boolean> allCachedScreenSettings;
    private String currentScreenName;
    private String lastScreenName;

    public NodeScreenManager() {
        this.allCachedScreens = new HashMap<>();
        this.allCachedScreenSettings = new HashMap<>();
        this.currentScreenName = null;
    }

    @Override
    public Task<Screen> getScreenByName(String name) {
        return Task.build(allCachedScreens.get(name));
    }

    @Override
    public Screen getScreenByNameOrNull(String name) {
        return allCachedScreens.get(name);
    }

    @Override
    public void joinScreen(Screen screen) {
        this.currentScreenName = screen.getName();
        if (lastScreenName == null) {
            this.lastScreenName = screen.getName();
        }

        if (CloudDriver.getInstance().getCommandManager() != null) {

            CloudDriver.getInstance().getCommandManager().setActive(this.allCachedScreenSettings.get(screen.getName()));
            CloudDriver.getInstance().getCommandManager().setInActiveHandler((ExceptionallyBiConsumer<CommandSender, String>) (sender, s) -> {
                for (Consumer<? super String> inputHandler : (getCurrentScreen() == null ? new ArrayList<Consumer<? super String>>() : new ArrayList<>(getCurrentScreen().getInputHandlers()))) {
                    inputHandler.accept(s);
                }
            });
        }



        screen.clear();

        if (CloudDriver.getInstance().getCommandSender() == null) {
            return;
        }

        //re-displaying old cached lines
        for (String allCachedLine : new ArrayList<>(screen.getAllCachedLines())) {
            CloudDriver.getInstance().getCommandSender().forceMessage(allCachedLine);
        }
    }

    @Override
    public void leaveCurrentScreen() {
        getCurrentScreen().getInputHandlers().clear();
        this.currentScreenName = null;

        CloudDriver.getInstance().getCommandManager().setInActiveHandler(null);
        CloudDriver.getInstance().getCommandManager().setActive(true);
        if (this.lastScreenName != null) {
            this.joinScreen(this.getScreenByNameOrNull(lastScreenName));
        }
    }

    @Override
    public boolean isScreenActive(String name) {
        return isCurrentlyInScreen() && (currentScreenName != null && currentScreenName.equalsIgnoreCase(name));
    }

    @Override
    public boolean isCurrentlyInScreen() {
        return getCurrentScreen() != null;
    }


    @Override
    public void registerScreen(String name, boolean enableCommandManager) {
        this.allCachedScreens.put(name, new NodeScreen(name));
        this.allCachedScreenSettings.put(name, enableCommandManager);
    }

    @Override
    public boolean isCurrentScreenAllowCommandManager() {
        return getCurrentScreen() != null && allCachedScreenSettings.get(getCurrentScreen().getName());
    }

    @Override
    public void unregisterScreen(String name) {
        this.allCachedScreens.remove(name);
    }

    public Screen getCurrentScreen() {
        return this.getScreenByNameOrNull(this.currentScreenName);
    }

    @Override
    public Collection<Screen> getRegisteredScreens() {
        return allCachedScreens.values();
    }
}
