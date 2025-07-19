package cloud.hytora.node.impl.command.impl;


import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.command.annotation.Command;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.driver.command.console.screen.Screen;
import cloud.hytora.driver.command.console.screen.ScreenManager;
import cloud.hytora.node.NodeDriver;

import java.util.function.Consumer;

@Command(
        value = "clear",
        description = "Clears the console",
        executionScope = CommandScope.CONSOLE
)

public class ClearCommand {

    @Command.Root
    public void executeClear(CommandSender sender) {
        NodeDriver.getInstance()
                .getProvider(ScreenManager.class)
                .update("console", new Consumer<Screen>() {
                    @Override
                    public void accept(Screen screen) {
                        screen.clear();
                        screen.clearCache();


                        screen.writeLine("§8");
                        screen.writeLine("§8");
                        screen.writeLine("%1    __  __      __                   ________                __");
                        screen.writeLine("%1   / / / /_  __/ /_____  _________ _/ ____/ /___  __  ______/ /");
                        screen.writeLine("%1  / /_/ / / / / __/ __ \\/ ___/ __ `/ /   / / __ \\/ / / / __  / ");
                        screen.writeLine("%1 / __  / /_/ / /_/ /_/ / /  / /_/ / /___/ / /_/ / /_/ / /_/ /  ");
                        screen.writeLine("%1/_/ /_/\\__, _____\\____/_/   \\__,_/\\____________/\\________,_/   ");
                        screen.writeLine("%1      /____/ ___/____ ___  __  _______/ __/  | |  / <  /       ");
                        screen.writeLine("%1 ______    \\__ \\/ __ `__ \\/ / / / ___/ /_    | | / // /  ______");
                        screen.writeLine("%1/_____/   ___/ / / / / / / /_/ / /  / __/    | |/ // /  /_____/");
                        screen.writeLine("%1         /____/_/ /_/ /_/\\__,_/_/  /_/       |___//_/          ");
                        screen.writeLine("§8");
                        screen.writeLine("§8==================================================");
                        screen.writeLine("§8");
                        screen.writeLine("§7The %1ConsoleScreen §7has been %2cleared§8.");
                        screen.writeLine("§8");
                        screen.writeLine("§8==================================================");
                    }
                });

    }
}
