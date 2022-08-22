package cloud.hytora.driver.commands.sender;

import cloud.hytora.common.logging.Logger;
import cloud.hytora.driver.console.Console;

import javax.annotation.Nonnull;


public interface ConsoleCommandSender extends CommandSender {

	@Nonnull
    Console getConsole();

	@Nonnull
    Logger getLogger();

}
