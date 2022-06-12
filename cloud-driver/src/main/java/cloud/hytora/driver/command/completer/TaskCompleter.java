package cloud.hytora.driver.command.completer;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.driver.services.task.ServiceTask;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.stream.Collectors;

public class TaskCompleter implements CommandCompleter {

    @NotNull
    @Override
    public Collection<String> complete(@NotNull CommandSender sender, @NotNull String message, @NotNull String argument) {
        return CloudDriver.getInstance().getServiceTaskManager().getAllCachedTasks().stream().map(ServiceTask::getName).collect(Collectors.toList());
    }
}
