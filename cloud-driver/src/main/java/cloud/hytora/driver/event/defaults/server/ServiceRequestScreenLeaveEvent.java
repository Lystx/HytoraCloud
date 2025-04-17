package cloud.hytora.driver.event.defaults.server;

import cloud.hytora.driver.command.CommandManager;
import cloud.hytora.driver.command.Console;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.driver.event.CloudEvent;
import cloud.hytora.driver.services.ICloudService;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ServiceRequestScreenLeaveEvent implements CloudEvent  {

    private final CommandManager commandManager;
    private final Console console;
    private final CommandSender sender;
    private final ICloudService service;
}
