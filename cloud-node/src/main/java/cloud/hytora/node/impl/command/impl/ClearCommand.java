package cloud.hytora.node.impl.command.impl;

import cloud.hytora.context.annotations.ApplicationParticipant;
import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.command.annotation.Command;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.driver.console.Screen;
import cloud.hytora.driver.console.ScreenManager;
import cloud.hytora.node.NodeDriver;

import java.util.function.Consumer;

@Command(
        value = "clear",
        description = "Clears the console",
        executionScope = CommandScope.CONSOLE
)
@ApplicationParticipant
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
                        screen.writeLine("§b    __  __      __                   ________                __");
                        screen.writeLine("§b   / / / /_  __/ /_____  _________ _/ ____/ /___  __  ______/ /");
                        screen.writeLine("§b  / /_/ / / / / __/ __ \\/ ___/ __ `/ /   / / __ \\/ / / / __  / ");
                        screen.writeLine("§b / __  / /_/ / /_/ /_/ / /  / /_/ / /___/ / /_/ / /_/ / /_/ /  ");
                        screen.writeLine("§b/_/ /_/\\__, _____\\____/_/   \\__,_/\\____________/\\________,_/   ");
                        screen.writeLine("§b      /____/ ___/____ ___  __  _______/ __/  | |  / <  /       ");
                        screen.writeLine("§b ______    \\__ \\/ __ `__ \\/ / / / ___/ /_    | | / // /  ______");
                        screen.writeLine("§b/_____/   ___/ / / / / / / /_/ / /  / __/    | |/ // /  /_____/");
                        screen.writeLine("§b         /____/_/ /_/ /_/\\__,_/_/  /_/       |___//_/          ");
                        screen.writeLine("§8");
                        screen.writeLine("§8==================================================");
                        screen.writeLine("§8");
                        screen.writeLine("§7The §bConsoleScreen §7has been §3cleared§8.");
                        screen.writeLine("§8");
                        screen.writeLine("§8==================================================");
                    }
                });

    }
}
