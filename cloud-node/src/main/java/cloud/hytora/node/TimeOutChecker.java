package cloud.hytora.node;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.node.INode;
import cloud.hytora.driver.node.INodeManager;
import cloud.hytora.driver.node.data.INodeCycleData;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.driver.services.ICloudServiceManager;
import cloud.hytora.driver.services.task.IServiceTask;
import cloud.hytora.driver.services.utils.ServiceState;
import cloud.hytora.driver.sync.ISyncedNetworkPromise;
import cloud.hytora.driver.sync.SyncedObjectType;

import java.util.*;


public class TimeOutChecker implements Runnable {

	@Override
	public synchronized void run() {
		this.checkNodeTimeout(); //checking nodes
		this.checkServiceTimeout(); //checking for service timeout
	}

	private void checkServiceTimeout() {
		for (ICloudServer service : CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudServiceManager.class).getAllServicesByState(ServiceState.ONLINE)) {
			if (!service.isTimedOut()) {
				continue; //if not timed out ignoring
			}
			CloudDriver.getInstance().getLogger().warn("{} has not sent the required info updates", service);

			IServiceTask serviceTask = service.getTask();
			if ((serviceTask == null || serviceTask.getTaskGroup().getShutdownBehaviour().isDynamic())) {
				CloudDriver.getInstance().getLogger().warn("=> Probably crashed -> Deleting..");
				CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudServiceManager.class).shutdownService(service);
				return;
			}

		}
	}

	private final List<String> highPingNodes = new ArrayList<>();

	private void checkNodeTimeout() {
		for (INode node : NodeDriver.getInstance().getProviderRegistry().getUnchecked(INodeManager.class).getAllCachedNodes()) {
			if (node.getName().equalsIgnoreCase(NodeDriver.getInstance().getNode().getName())) {
				continue;
			}
			INodeCycleData cycleData = node.getLastCycleData();
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
