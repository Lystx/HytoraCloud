package cloud.hytora.node;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.HytoraCloudConstants;
import cloud.hytora.driver.node.INode;
import cloud.hytora.driver.node.data.INodeData;
import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.driver.services.task.IServiceTask;
import cloud.hytora.driver.services.utils.ServiceState;

import java.text.SimpleDateFormat;
import java.util.*;


public class TimeOutChecker implements Runnable {

    @Override
    public synchronized void run() {
        this.checkNodeTimeout(); //checking nodes
        this.checkServiceTimeout(); //checking for service timeout
    }

    private void checkServiceTimeout() {
        for (ICloudService service : NodeDriver.getInstance().getServiceManager().getAllServicesByState(ServiceState.ONLINE)) {
            if (!service.isTimedOut()) {
                continue; //if not timed out ignoring
            }
            CloudDriver.getInstance().getLogger().warn("§7The Service §8'§c{}§8' §7has timed out and will now be stopped§8. [§cLost-Cycles: {}, §cLast-Sync: {}§8]", service.getName(), HytoraCloudConstants.SERVER_MAX_LOST_CYCLES, new SimpleDateFormat("HH:mm:ss").format(service.getLastCycleData().getTimestamp()));
            CloudDriver.getInstance().getServiceManager().shutdownService(service);
            return;
        }
    }

    private final List<String> highPingNodes = new ArrayList<>();

    private void checkNodeTimeout() {
        for (INode node : NodeDriver.getInstance().getNodeManager().getAllCachedNodes()) {
            if (node.getName().equalsIgnoreCase(NodeDriver.getInstance().getNode().getName())) {
                continue;
            }
            INodeData cycleData = node.getLastCycleData();
            if (cycleData == null) {
                continue;
            }

            if (cycleData.hasTimedOut()) {
                // TODO: 20.04.2025 force disconnect of node
                CloudDriver.getInstance().getLogger().warn("§7The Node §8'§c{}§8' §7has probably timed out§8!", node.getName());
                continue;
            }
            if (cycleData.getLatency() > 100) {
                if (highPingNodes.contains(node.getName())) {
                    return;
                }
                CloudDriver.getInstance().getLogger().warn("§7The Node §8'§6{}§8' §7has a hard time trying to keep its connection alive §8[§ePing§8: §e{}ms§8]", node.getName(), cycleData.getLatency());
                highPingNodes.add(node.getName());
            } else {
                if (highPingNodes.contains(node.getName())) {
                    highPingNodes.remove(node.getName());
                    CloudDriver.getInstance().getLogger().warn("§7The Node §8'§a{}§8' §7has stabilized its connection §8[§ePing§8: §e{}ms§8]", node.getName(), cycleData.getLatency());
                }
            }
        }
    }

}
