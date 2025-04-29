package cloud.hytora.node.service.helper;

import cloud.hytora.driver.CloudDriver;


import cloud.hytora.driver.node.INode;
import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.driver.services.impl.UniversalCloudServer;
import cloud.hytora.driver.services.task.IServiceTask;
import cloud.hytora.driver.services.utils.ServiceState;
import cloud.hytora.node.NodeDriver;
import cloud.hytora.node.impl.config.MainConfiguration;
import lombok.Getter;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

@Getter
public class NodeServiceQueue {

    private final int maxBootableServices;

    private final Collection<String> pausedGroups;

    public NodeServiceQueue() {
        this.maxBootableServices = NodeDriver.getInstance().getNode().getConfig().getMaxBootableServicesAtSameTime();
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

        CloudDriver.getInstance()
                .getServiceManager()
                .getAllCachedServices()
                .stream()
                .filter(ser -> ser.getServiceState() == ServiceState.PREPARED)
                .findFirst()
                .ifPresent(cloudServer -> {
                    cloudServer
                            .getTask()
                            .findAnyNodeAsync()
                            .onTaskFailed(error -> {
                                CloudDriver.getInstance().getLogger().error("Tried to start {} but the Node(s) {} for Servers of Configuration {} is not connected!", cloudServer.getName(), cloudServer.getTask().getPossibleNodes(), cloudServer.getTask().getName());
                            })
                            .onTaskSucess(node -> {
                                if (!node.hasEnoughMemoryToStart(cloudServer)) {
                                    CloudDriver.getInstance().getLogger().warn("'{}' couldn't start {} because its maximum memory of {} has been reached!", node.getName(), cloudServer.getName(), node.getConfig().getMemory());
                                    return;
                                }
                                node.startServer(cloudServer);
                                dequeue();
                            });
                });

    }

    private void queue() {
        CloudDriver.getInstance().getServiceTaskManager().getAllCachedTasks().stream()
                .filter(con -> this.getAmountOfGroupServices(con) < con.getMinOnlineService())
                .filter(con -> pausedGroups.stream().noneMatch(s -> s.equalsIgnoreCase(con.getName())))
                .sorted(Comparator.comparingInt(IServiceTask::getStartOrder))
                .forEach(task -> {

                    INode node = task.findAnyNode();

                    if (node == null) {
                        CloudDriver.getInstance().getLogger().info("Tried to start a Service of Group '" + task.getName() + "' but no Node(s) with name '" + task.getPossibleNodes() + "' is connected!");
                        return;
                    }


                    int port = task.getVersion().isProxy() ? MainConfiguration.getInstance().getProxyStartPort() : MainConfiguration.getInstance().getSpigotStartPort();
                    while (isPortUsed(port)) {
                        port++;
                    }

                    ICloudService service = new UniversalCloudServer(task.getName(), this.getPossibleServiceIDByGroup(task), port, node.getConfig().getAddress().getHost());
                    service.setRunningNodeName(node.getName());
                    CloudDriver.getInstance().getServiceManager().registerService(service);

                });
    }

    private boolean minBootableServiceExists() {
        return this.getAmountOfBootableServices() >= maxBootableServices;
    }

    private int getAmountOfBootableServices() {
        return CloudDriver.getInstance().getServiceManager().getAllServicesByState(ServiceState.STARTING).size();
    }

    public int getAmountOfGroupServices(IServiceTask serviceGroup) {
        return (int) CloudDriver.getInstance().getServiceManager().getAllCachedServices().stream()
                .filter(it -> it.getTask().equals(serviceGroup)).count();
    }

    private int getPossibleServiceIDByGroup(IServiceTask serviceGroup) {
        int id = 1;
        while (this.isServiceIDAlreadyExists(serviceGroup, id)) id++;
        return id;
    }

    private boolean isServiceIDAlreadyExists(IServiceTask serviceGroup, int id) {
        return CloudDriver.getInstance().getServiceManager().getAllServicesByTask(serviceGroup).stream().anyMatch(it -> id == it.getServiceID());
    }

    private boolean isPortUsed(int port) {
        for (ICloudService service : NodeDriver.getInstance().getServiceManager().getAllCachedServices()) {
            if (service.getTask().getPossibleNodes().stream().anyMatch(n -> n.equalsIgnoreCase(NodeDriver.getInstance().getExecutor().getNodeName()))) {
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
