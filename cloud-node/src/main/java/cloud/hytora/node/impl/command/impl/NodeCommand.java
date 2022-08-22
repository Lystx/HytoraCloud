package cloud.hytora.node.impl.command.impl;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.commands.context.CommandContext;
import cloud.hytora.driver.commands.data.Command;
import cloud.hytora.driver.commands.data.enums.AllowedCommandSender;
import cloud.hytora.driver.commands.data.enums.CommandScope;
import cloud.hytora.driver.commands.help.ArgumentHelp;
import cloud.hytora.driver.commands.help.ArgumentHelper;
import cloud.hytora.driver.commands.parameter.CommandArguments;
import cloud.hytora.driver.commands.tabcomplete.TabCompleter;
import cloud.hytora.driver.commands.tabcomplete.TabCompletion;
import cloud.hytora.driver.node.INode;
import cloud.hytora.driver.node.INodeManager;
import cloud.hytora.node.NodeDriver;

import java.text.DecimalFormat;
import java.util.List;

@Command(
        label = "node",
        aliases = {"nodes"},
        desc = "Manages all nodes",
        invalidUsageIfEmptyInput = true,
        autoHelpAliases = {"help", "?"}
)
public class NodeCommand {

    private final INodeManager nodeManager = NodeDriver.getInstance().getProviderRegistry().getUnchecked(INodeManager.class);

    @ArgumentHelp
    public void onArgumentHelp(ArgumentHelper helper) {
        helper.performTemplateHelp();
    }

    @TabCompletion
    public void onTabComplete(TabCompleter completer) {
        completer.reactWithSubCommands("node");
    }

    @Command(
            label = "list",
            parent = "node",
            desc = "Lists all nodes",
            scope = CommandScope.CONSOLE_AND_INGAME
    )
    public void listCommand(CommandContext<?> ctx, CommandArguments args) {
        List<INode> allConnectedNodes = nodeManager.getAllCachedNodes();

        if (allConnectedNodes.isEmpty()) {
            ctx.sendMessage("§cThere are no other connected Nodes at the moment!");
            return;
        }

        ctx.sendMessage("§8");
        ctx.sendMessage("§7Nodes§8:");

        for (INode node : allConnectedNodes) {
            ctx.sendMessage("§b" + node.getName() + " §8[§e" + node.getRunningServers().size() + " Servers §8| §e" + node.getLastCycleData().getLatency() + "ms§8]");
        }
        ctx.sendMessage("§8");
    }


    @Command(
            label = "info",
            usage = "<node>",
            parent = "node",
            desc = "Gives info about a node",
            scope = CommandScope.CONSOLE_AND_INGAME
    )
    public void infoCommand(CommandContext<?> ctx, CommandArguments args) {

        INode node = args.get(0, INode.class);

        if (node == null) {
            ctx.sendMessage("§cThere is no such connected Node!");
            return;
        }

        ctx.sendMessage("§8");
        ctx.sendMessage("§7Info for '§b" + node.getName() + "'§8:");
        ctx.sendMessage("§bRunning Servers: §7" + node.getRunningServers().size());
        ctx.sendMessage("§bRemote: §7" + (node.getConfig().isRemote() ? "§aYes" : "§cNo"));
        ctx.sendMessage("§bPing: §7" + node.getLastCycleData().getLatency() + "ms");
        ctx.sendMessage("§bCores: §7" + node.getLastCycleData().getCores());
        ctx.sendMessage("§bCPU: §7" + new DecimalFormat("##.##").format(node.getLastCycleData().getCpuUsage()) + "%");
        ctx.sendMessage("§bRAM: §7" + node.getLastCycleData().getFreeRam() + "/" + node.getLastCycleData().getMaxRam());
        ctx.sendMessage("§8");
    }


    @Command(
            label = "stop",
            usage = "<node>",
            parent = "node",
            desc = "Shuts down a node",
            scope = CommandScope.CONSOLE_AND_INGAME
    )
    public void stopCommand(CommandContext<?> ctx, CommandArguments args) {
        INode node = args.get(0, INode.class);

        if (node == null) {
            ctx.sendMessage("§cThere is no such connected Node!");
            return;
        }
        ctx.sendMessage("§7Requesting §cshutdown §7of §8'§b" + node.getName() + "§8'...");
        node.shutdown();
    }

}
