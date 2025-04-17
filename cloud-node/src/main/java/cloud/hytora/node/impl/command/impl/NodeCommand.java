package cloud.hytora.node.impl.command.impl;

import cloud.hytora.context.annotations.ApplicationParticipant;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.command.annotation.*;
import cloud.hytora.driver.command.completer.impl.NodeCompleter;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.driver.node.INode;
import cloud.hytora.driver.node.NodeManager;

import java.text.DecimalFormat;
import java.util.List;

@Command(
        value = {"nodes", "node"},
        permission = "cloud.command.use",
        description = "Manages all Nodes",
        executionScope = CommandScope.CONSOLE_AND_INGAME
)
@Command.AutoHelp
@ApplicationParticipant
public class NodeCommand {

    private final NodeManager nodeManager = CloudDriver.getInstance().getNodeManager();

    @Command(value = "list", description = "Lists all nodes")
    public void executeList(CommandSender sender) {
        List<INode> allConnectedNodes = nodeManager.getAllCachedNodes();

        if (allConnectedNodes.isEmpty()) {
            sender.sendMessage("§cThere are no other connected Nodes at the moment!");
            return;
        }

        sender.sendMessage("§8");
        sender.sendMessage("§7Nodes§8:");

        for (INode node : allConnectedNodes) {
            sender.sendMessage("§b" + node.getName() + " §8[§e" + node.getRunningServers().size() + " Servers §8| §e" + node.getLastCycleData().getLatency() + "ms§8]");
        }
        sender.sendMessage("§8");
    }


    @Command(value = "info", description = "Gives info on a Node")
    @Command.Syntax("<name>")
    public void executeInfo(CommandSender sender, @Command.Argument(value = "name", completer = NodeCompleter.class) INode node) {
        if (node == null) {
            sender.sendMessage("§cThere is no such connected Node!");
            return;
        }

        sender.sendMessage("§8");
        sender.sendMessage("§7Info for '§b" + node.getName() + "'§8:");
        sender.sendMessage("§bRunning Servers: §7" + node.getRunningServers().size());
        sender.sendMessage("§bRemote: §7" + (node.getConfig().isRemote() ? "§aYes" : "§cNo"));
        sender.sendMessage("§bPing: §7" + node.getLastCycleData().getLatency() + "ms");
        sender.sendMessage("§bCores: §7" + node.getLastCycleData().getCores());
        sender.sendMessage("§bCPU: §7" + new DecimalFormat("##.##").format(node.getLastCycleData().getCpuUsage()) + "%");
        sender.sendMessage("§bRAM: §7" + node.getLastCycleData().getFreeRam() + "/" + node.getLastCycleData().getMaxRam());
        sender.sendMessage("§8");
    }


    @Command(value = "stop", description = "Stops a Node")
    @Command.Syntax("<name>")
    public void executeStop(CommandSender sender, @Command.Argument(value = "name", completer = NodeCompleter.class) INode node) {
        if (node == null) {
            sender.sendMessage("§cThere is no such connected Node!");
            return;
        }
        sender.sendMessage("§7Requesting §cshutdown §7of §8'§b" + node.getName() + "§8'...");
        node.shutdown();
    }

}
