package cloud.hytora.driver.command;

import cloud.hytora.common.logging.Logger;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.command.sender.ConsoleCommandSender;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@Getter @RequiredArgsConstructor
public class DefaultCommandSender implements ConsoleCommandSender {

    private final String name;
    private final Console console;

    /**
     * The consumer to send a message
     */
    private Consumer<String> messageSending;

    @Override
    public void sendMessage(@NotNull String message) {
        if (messageSending != null) {
            messageSending.accept(message);
            return;
        }
        getLogger().info(message);
    }

    public ConsoleCommandSender function(Consumer<String> messageSending) {
        this.messageSending = messageSending;
        return this;
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        return true;
    }

    @NotNull
    @Override
    public Logger getLogger() {
        return CloudDriver.getInstance().getLogger();
    }
}
