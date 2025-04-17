package cloud.hytora.node;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.node.INode;
import cloud.hytora.driver.node.data.INodeData;
import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.driver.services.task.IServiceTask;
import cloud.hytora.driver.services.utils.ServiceState;

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
			CloudDriver.getInstance().getLogger().warn("{} has not sent the required info updates", service);

			IServiceTask serviceTask = service.getTask();
			if ((serviceTask == null || serviceTask.getTaskGroup().getShutdownBehaviour().isDynamic())) {
				CloudDriver.getInstance().getLogger().warn("=> Probably crashed -> Deleting..");
				CloudDriver.getInstance().getServiceManager().shutdownService(service);
				return;
			}
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
				CloudDriver.getInstance().getLogger().warn("'{}' has timeouted!", node.getName());
				continue;
			}
			if (cycleData.getLatency() > 100) {
				if (highPingNodes.contains(node.getName())) {
					return;
				}
				CloudDriver.getInstance().getLogger().warn("'{}' took {}ms to keep its connection alive.", node.getName(), cycleData.getLatency());
				highPingNodes.add(node.getName());
			} else {
				if (highPingNodes.contains(node.getName())) {
					highPingNodes.remove(node.getName());
					CloudDriver.getInstance().getLogger().info("'{}' has stabilized its connection ({}ms)", node.getName(), cycleData.getLatency());
				}
			}
		}
	}

}
