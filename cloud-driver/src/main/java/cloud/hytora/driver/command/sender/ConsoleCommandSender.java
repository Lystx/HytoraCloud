package cloud.hytora.driver.command.sender;

import cloud.hytora.common.logging.Logger;
import cloud.hytora.driver.command.Console;

import javax.annotation.Nonnull;


public interface ConsoleCommandSender extends CommandSender {

	@Nonnull
    Console getConsole();

	@Nonnull
    Logger getLogger();

}
