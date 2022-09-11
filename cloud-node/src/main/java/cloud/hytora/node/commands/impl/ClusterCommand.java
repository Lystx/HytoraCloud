package cloud.hytora.node.commands.impl;

import cloud.hytora.common.scheduler.Scheduler;
import cloud.hytora.driver.commands.context.CommandContext;
import cloud.hytora.driver.commands.data.Command;
import cloud.hytora.driver.commands.data.enums.CommandScope;
import cloud.hytora.driver.commands.help.CommandHelp;
import cloud.hytora.driver.commands.help.CommandHelper;
import cloud.hytora.driver.commands.parameter.CommandArguments;
import cloud.hytora.driver.commands.tabcomplete.TabCompleter;
import cloud.hytora.driver.commands.tabcomplete.TabCompletion;
import cloud.hytora.driver.node.INode;
import cloud.hytora.driver.node.INodeManager;
import cloud.hytora.node.NodeDriver;

@Command(
        label = "cluster",
        aliases = "cl",
        desc = "Manages the cluster",
        invalidUsageIfEmptyInput = true,
        autoHelpAliases = {"help", "?"},
        permission = "cloud.command.cluster",
        scope = CommandScope.CONSOLE_AND_INGAME
)
// TODO: 21.08.2022 implement more
public class ClusterCommand {

    @CommandHelp
    public void onArgumentHelp(CommandHelper helper) {
        helper.performTemplateHelp();
    }

    @TabCompletion
    public void onTabComplete(TabCompleter completer) {
        completer.reactWithSubCommands("cluster");
    }

    @Command(
            label = "shutdown",
            parent = "cluster",
            desc = "Shuts down the cluster",
            scope = CommandScope.CONSOLE_AND_INGAME
    )
    public void shutdownCommand(CommandContext<?> ctx, CommandArguments args) {
        ctx.sendMessage("Sending Shutdown-Request to every ClusterParticipant and then shutting down HeadNode after 1 second...");
        INode headNode = NodeDriver.getInstance().getProviderRegistry().getUnchecked(INodeManager.class).getHeadNode();
        for (INode node: NodeDriver.getInstance().getProviderRegistry().getUnchecked(INodeManager.class).getAllCachedNodes()) {
            if (node.getName().equalsIgnoreCase(headNode.getName())) {
                continue; //headNode can't be shut down before all other nodes are shut down
            }
            node.shutdown();
        }
        Scheduler.runTimeScheduler().scheduleDelayedTask(headNode::shutdown, 20L);
        if (NodeDriver.getInstance().getProviderRegistry().getUnchecked(INodeManager.class).isHeadNode()) {
            return;
        }
        Scheduler.runTimeScheduler().scheduleDelayedTask(() -> NodeDriver.getInstance().shutdown(), 40L);
    }


}
