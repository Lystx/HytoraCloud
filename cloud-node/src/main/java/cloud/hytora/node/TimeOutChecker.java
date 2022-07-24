package cloud.hytora.node;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.node.Node;
import cloud.hytora.driver.node.NodeCycleData;
import cloud.hytora.driver.services.ServiceInfo;
import cloud.hytora.driver.services.task.ServiceTask;
import cloud.hytora.driver.services.utils.ServiceState;

import java.util.*;


public class TimeOutChecker implements Runnable {

	@Override
	public synchronized void run() {
		// TODO: 23.05.2022 check why not work properly
		//this.checkNodeTimeout(); //checking nodes
		//this.checkServiceTimeout(); //checking for service timeout
	}

	private void checkServiceTimeout() {
		for (ServiceInfo service : NodeDriver.getInstance().getServiceManager().getAllCachedServices()) {
			if (!service.isTimedOut()) {
				continue; //if not timed out ignoring
			}
			CloudDriver.getInstance().getLogger().warn("{} has not sent the required info updates", service);

			ServiceTask serviceTask = service.getTask();
			if (service.getServiceState() == ServiceState.ONLINE && (serviceTask == null || serviceTask.getTaskGroup().getShutdownBehaviour().isDynamic())) {
				CloudDriver.getInstance().getLogger().warn("=> Probably crashed -> Deleting..");
				CloudDriver.getInstance().getServiceManager().shutdownService(service);
				return;
			}
		}
	}

	private final List<String> highPingNodes = new ArrayList<>();

	private void checkNodeTimeout() {
		for (Node node : NodeDriver.getInstance().getNodeManager().getAllConnectedNodes()) {
			if (node.getName().equalsIgnoreCase(NodeDriver.getInstance().getName())) {
				continue;
			}
			NodeCycleData cycleData = node.getLastCycleData();
			if (cycleData == null) {
				continue;
			}

			if (cycleData.hasTimedOut()) {
				CloudDriver.getInstance().getLogger().warn("'{}' has timeouted!", node.getName());
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
