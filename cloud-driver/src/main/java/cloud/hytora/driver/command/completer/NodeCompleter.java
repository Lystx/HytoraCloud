package cloud.hytora.driver.command.completer;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.driver.node.INode;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.stream.Collectors;

public class NodeCompleter implements CommandCompleter {

    @NotNull
    @Override
    public Collection<String> complete(@NotNull CommandSender sender, @NotNull String message, @NotNull String argument) {
        return CloudDriver.getInstance().getNodeManager().getAllCachedNodes().stream().map(INode::getName).collect(Collectors.toList());
    }
}
