package cloud.hytora.node.service.helper;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;


import cloud.hytora.driver.node.Node;
import cloud.hytora.driver.node.NodeManager;
import cloud.hytora.driver.services.ServiceInfo;
import cloud.hytora.driver.services.impl.SimpleServiceInfo;
import cloud.hytora.driver.services.task.ServiceTask;
import cloud.hytora.driver.services.utils.ServiceState;
import cloud.hytora.node.NodeDriver;
import cloud.hytora.node.impl.config.MainConfiguration;
import cloud.hytora.driver.networking.cluster.ClusterClientExecutor;
import lombok.Getter;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@Getter
public class NodeServiceQueue {

    private static int MAX_BOOTABLE_SERVICES = 2;

    private final Collection<String> pausedGroups;

    public NodeServiceQueue() {
        MAX_BOOTABLE_SERVICES = NodeDriver.getInstance().getConfig().getMaxBootableServicesAtSameTime();
        this.pausedGroups = new ArrayList<>();

        this.dequeue();
    }

    public void dequeue() {
        if (!NodeDriver.getInstance().isRunning()) {
            return;
        }

        this.queue();

        if (this.minBootableServiceExists()) {
            return;
        }

        List<ServiceInfo> services = CloudDriver.getInstance().getServiceManager().getAllServicesByState(ServiceState.PREPARED);

        if (services.isEmpty()) {
            CloudDriver.getInstance().getLogger().info("§6There are no more prepared services to start!");
            return;
        }
        ServiceInfo serviceInfo = services.get(0);
        NodeManager nodeManager = NodeDriver.getInstance().getNodeManager();
        Task<Node> node = nodeManager.getNode(serviceInfo.getTask().getNode());

        node.ifPresent(n -> n.startServer(serviceInfo));
        node.ifEmpty(n -> CloudDriver.getInstance().getLogger().error("Tried to start {} but the Node {} for Servers of Configuration {} is not connected!", serviceInfo.getName(), serviceInfo.getTask().getNode(), serviceInfo.getTask().getName()));

    }

    private void queue() {
        CloudDriver.getInstance().getServiceTaskManager().getAllCachedTasks().stream()
                .filter(con -> this.getAmountOfGroupServices(con) < con.getMinOnlineService())
                .filter(con -> !pausedGroups.contains(con.getName()))
                .sorted(Comparator.comparingInt(ServiceTask::getStartOrder))
                .forEach(task -> {

                    ClusterClientExecutor nodeClient = NodeDriver.getInstance().getExecutor().getClient(task.getNode()).orElse(null);
                    boolean thisSidesNode = task.getNode().equalsIgnoreCase(NodeDriver.getInstance().getExecutor().getNodeName());

                    if (nodeClient == null && !thisSidesNode) {
                        CloudDriver.getInstance().getLogger().info("Tried to start a Service of Group '" + task.getName() + "' but no Node with name '" + task.getNode() + "' is connected!");
                        return;
                    }

                    String address = thisSidesNode ? "127.0.0.1" : ((InetSocketAddress) nodeClient.getChannel().remoteAddress()).getAddress().getHostAddress();

                    int port = task.getVersion().isProxy() ? MainConfiguration.getInstance().getProxyStartPort() : MainConfiguration.getInstance().getSpigotStartPort();
                    while (isPortUsed(port)) {
                        port++;
                    }

                    ServiceInfo service = new SimpleServiceInfo(task.getName(), this.getPossibleServiceIDByGroup(task), port, address);
                    CloudDriver.getInstance().getServiceManager().registerService(service);

                });
    }

    private boolean minBootableServiceExists() {
        return this.getAmountOfBootableServices() >= MAX_BOOTABLE_SERVICES;
    }

    private int getAmountOfBootableServices() {
        return CloudDriver.getInstance().getServiceManager().getAllServicesByState(ServiceState.STARTING).size();
    }

    public int getAmountOfGroupServices(ServiceTask serviceGroup) {
        return (int) CloudDriver.getInstance().getServiceManager().getAllCachedServices().stream()
                .filter(it -> it.getTask().equals(serviceGroup)).count();
    }

    private int getPossibleServiceIDByGroup(ServiceTask serviceGroup) {
        int id = 1;
        while (this.isServiceIDAlreadyExists(serviceGroup, id)) id++;
        return id;
    }

    private boolean isServiceIDAlreadyExists(ServiceTask serviceGroup, int id) {
        return CloudDriver.getInstance().getServiceManager().getAllServicesByGroup(serviceGroup).stream().anyMatch(it -> id == it.getServiceID());
    }

    private boolean isPortUsed(int port) {
        for (ServiceInfo service : NodeDriver.getInstance().getServiceManager().getAllCachedServices()) {
            if (service.getTask().getNode().equals(NodeDriver.getInstance().getExecutor().getNodeName())) {
                if (service.getPort() == port) {
                    return true;
                }
            }
        }
        try (ServerSocket serverSocket = new ServerSocket()) {
            serverSocket.bind(new InetSocketAddress(port));
            return false;
        } catch (Exception exception) {
            return true;
        }
    }
}
