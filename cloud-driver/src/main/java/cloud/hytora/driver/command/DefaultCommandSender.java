package cloud.hytora.driver.command;

import cloud.hytora.common.logging.Logger;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.DriverEnvironment;
import cloud.hytora.driver.command.sender.ConsoleCommandSender;
import cloud.hytora.driver.common.CloudMessages;
import cloud.hytora.driver.component.style.ComponentColor;
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

    /**
     * The consumer to send a message
     */
    private Consumer<String> forceSending;
    @Override
    public void sendMessage(@NotNull String message) {
        if (CloudDriver.getInstance().getEnvironment() == DriverEnvironment.NODE) {
            message = ComponentColor.translateColorCodes('§', message);
            CloudMessages messages = CloudMessages.getInstance();
            if (messages != null) {
                message = message.replace(messages.getPrefix(), "");
            }
        }
        if (messageSending != null) {
            messageSending.accept(message);
            return;
        }
        getLogger().info(message);
    }

    @Override
    public void forceMessage(@NotNull String message) {
        this.forceSending.accept(message);
    }

    public DefaultCommandSender function(Consumer<String> messageSending) {
        this.messageSending = messageSending;
        return this;
    }

    public DefaultCommandSender forceFunction(Consumer<String> forceSending) {
        this.forceSending = forceSending;
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
