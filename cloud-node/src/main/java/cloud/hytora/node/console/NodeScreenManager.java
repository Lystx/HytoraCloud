package cloud.hytora.node.console;

import cloud.hytora.common.function.ExceptionallyBiConsumer;
import cloud.hytora.common.task.ITask;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.commands.ICommandManager;
import cloud.hytora.driver.commands.sender.CommandSender;
import cloud.hytora.driver.console.screen.Screen;
import cloud.hytora.driver.console.screen.ScreenManager;
import cloud.hytora.node.NodeDriver;

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
    public ITask<Screen> getScreenByName(String name) {
        return ITask.newInstance(allCachedScreens.get(name));
    }

    @Override
    public Screen getScreenByNameOrNull(String name) {
        return allCachedScreens.get(name);
    }

    @Override
    public void joinScreen(Screen screen) {
        this.currentScreenName = screen.getName();
        this.lastScreenName = screen.getName();


        CloudDriver.getInstance()
                .getProviderRegistry()
                .getUnchecked(ICommandManager.class)
                .setActive(
                        this.allCachedScreenSettings.get(screen.getName()),
                        (ExceptionallyBiConsumer<CommandSender, String>) (commandSender, s) -> {
                            for (Consumer<? super String> inputHandler : (getCurrentScreen() == null ? new ArrayList<Consumer<? super String>>() : new ArrayList<>(getCurrentScreen().getInputHandlers()))) {
                                inputHandler.accept(s);
                            }
                        });


        screen.clear();

        //re-displaying old cached lines
        for (String allCachedLine : new ArrayList<>(screen.getAllCachedLines())) {
            NodeDriver.getInstance().getCommandSender().forceMessage(allCachedLine);
        }
    }

    @Override
    public void leaveCurrentScreen() {
        if (getCurrentScreen() != null) {
            getCurrentScreen().getInputHandlers().clear();
        }
        this.currentScreenName = null;

        CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICommandManager.class).setActive(true, null);
        if (this.lastScreenName != null) {
            System.out.println("Joining back to " + lastScreenName);
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
    public Screen registerScreen(String name, boolean enableCommandManager) {
        NodeScreen screen = new NodeScreen(name);
        this.allCachedScreens.put(name, screen);
        this.allCachedScreenSettings.put(name, enableCommandManager);

        return screen;
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
