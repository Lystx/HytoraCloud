package cloud.hytora.node.impl.command.impl;

import cloud.hytora.context.annotations.ApplicationParticipant;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.command.annotation.*;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.driver.node.INode;
import cloud.hytora.node.NodeDriver;

@Command({"cluster", "cl"})
@CommandExecutionScope(CommandScope.CONSOLE_AND_INGAME)
@CommandPermission("cloud.command.use")
@CommandDescription("Manages the Cluster")
@CommandAutoHelp
@ApplicationParticipant
public class ClusterCommand {

    @Command("shutdown")
    @CommandDescription("Shuts down the whole cluster")
    public void executeShutdown(CommandSender sender) {
        sender.sendMessage("Sending Shutdown-Request to every ClusterParticipant and then shutting down HeadNode after 1 second...");
        INode headNode = CloudDriver.getInstance().getNodeManager().getHeadNode();
        for (INode node: CloudDriver.getInstance().getNodeManager().getAllCachedNodes()) {
            if (node.getName().equalsIgnoreCase(headNode.getName())) {
                continue; //headNode can't be shut down before all other nodes are shut down
            }
            node.shutdown();
        }
        CloudDriver.getInstance().getScheduler().scheduleDelayedTask(headNode::shutdown, 20L);
        if (CloudDriver.getInstance().getNodeManager().isHeadNode()) {
            return;
        }
        CloudDriver.getInstance().getScheduler().scheduleDelayedTask(() -> NodeDriver.getInstance().shutdown(), 40L);
    }


    @Command("publish all")
    @CommandDescription("Publishes every data to the whole cluster")
    public void executePublishAll(CommandSender sender) {

    }

    @Command("publish modules")
    @CommandDescription("Publishes all modules to the whole cluster")
    public void executePublishModules(CommandSender sender) {

    }
}
