package cloud.hytora.node.impl.command.impl;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.command.annotation.*;
import cloud.hytora.driver.command.completer.NodeCompleter;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.driver.node.Node;
import cloud.hytora.driver.node.NodeManager;

import java.text.DecimalFormat;
import java.util.List;

@Command({"nodes", "node"})
@CommandExecutionScope(CommandScope.CONSOLE_AND_INGAME)
@CommandPermission("cloud.command.use")
@CommandAutoHelp
@CommandDescription("Manages all Nodes")
public class NodeCommand {

    private final NodeManager nodeManager = CloudDriver.getInstance().getNodeManager();

    @Command("list")
    @CommandDescription("Lists all nodes")
    public void executeList(CommandSender sender) {
        List<Node> allConnectedNodes = nodeManager.getAllConnectedNodes();

        if (allConnectedNodes.isEmpty()) {
            sender.sendMessage("§cThere are no other connected Nodes at the moment!");
            return;
        }

        sender.sendMessage("§8");
        sender.sendMessage("§7Nodes§8:");

        for (Node node : allConnectedNodes) {
            sender.sendMessage("§b" + node.getName() + " §8[§e" + node.getRunningServers().size() + " Servers §8| §e" + node.getLastCycleData().getLatency() + "ms§8]");
        }
        sender.sendMessage("§8");
    }


    @Command("info")
    @Syntax("<name>")
    @CommandDescription("Gives info on a Node")
    public void executeInfo(CommandSender sender, @Argument(value = "name", completer = NodeCompleter.class) Node node) {
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


    @Command("stop")
    @Syntax("<name>")
    @CommandDescription("Stops a Node")
    public void executeStop(CommandSender sender, @Argument(value = "name", completer = NodeCompleter.class) Node node) {
        if (node == null) {
            sender.sendMessage("§cThere is no such connected Node!");
            return;
        }
        sender.sendMessage("§7Requesting §cshutdown §7of §8'§b" + node.getName() + "§8'...");
        node.shutdown();
    }

}
