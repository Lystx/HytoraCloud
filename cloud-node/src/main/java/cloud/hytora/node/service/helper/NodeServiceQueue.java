package cloud.hytora.node.service.helper;

import cloud.hytora.common.wrapper.Wrapper;
import cloud.hytora.driver.CloudDriver;


import cloud.hytora.driver.networking.packets.services.CloudServerCacheRegisterPacket;
import cloud.hytora.driver.node.Node;
import cloud.hytora.driver.node.NodeManager;
import cloud.hytora.driver.services.CloudServer;
import cloud.hytora.driver.services.impl.SimpleCloudServer;
import cloud.hytora.driver.services.configuration.ServerConfiguration;
import cloud.hytora.driver.services.utils.ServiceState;
import cloud.hytora.node.NodeDriver;
import cloud.hytora.node.impl.config.MainConfiguration;
import cloud.hytora.driver.networking.cluster.ClusterClientExecutor;
import cloud.hytora.node.service.NodeServiceManager;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class NodeServiceQueue {

    private static final int MAX_BOOTABLE_SERVICES = 2;


    public void dequeue() {
        if (!NodeDriver.getInstance().isRunning()) {
            return;
        }

        this.queue();

        if (this.minBootableServiceExists()) {
            return;
        }

        List<CloudServer> services = CloudDriver.getInstance().getServiceManager().getAllServicesByState(ServiceState.PREPARED);

        if (services.isEmpty()) {
            CloudDriver.getInstance().getLogger().info("§6There are no more prepared services to start!");
            return;
        }
        CloudServer cloudServer = services.get(0);
        NodeManager nodeManager = NodeDriver.getInstance().getNodeManager();
        Wrapper<Node> node = nodeManager.getNode(cloudServer.getConfiguration().getNode());

        node.ifPresent(n -> n.startServer(cloudServer));
        node.ifEmpty(n -> CloudDriver.getInstance().getLogger().error("Tried to start {} but the Node {} for Servers of Configuration {} is not connected!", cloudServer.getName(), cloudServer.getConfiguration().getNode(), cloudServer.getConfiguration().getName()));

    }

    public void queue() {
        CloudDriver.getInstance().getConfigurationManager().getAllCachedConfigurations().stream()
                .filter(serviceGroup -> this.getAmountOfGroupServices(serviceGroup) < serviceGroup.getMinOnlineService())
                .sorted(Comparator.comparingInt(ServerConfiguration::getStartOrder))
                .forEach(serviceGroup -> {

                    ClusterClientExecutor nodeClient = NodeDriver.getInstance().getExecutor().getClient(serviceGroup.getNode()).orElse(null);
                    boolean thisSidesNode = serviceGroup.getNode().equalsIgnoreCase(NodeDriver.getInstance().getExecutor().getNodeName());

                    if (nodeClient == null && !thisSidesNode) {
                        CloudDriver.getInstance().getLogger().info("Tried to start a Service of Group '" + serviceGroup.getName() + "' but no Node with name '" + serviceGroup.getNode() + "' is connected!");
                        return;
                   }

                    String address = thisSidesNode ? "127.0.0.1" : ((InetSocketAddress)nodeClient.getChannel().remoteAddress()).getAddress().getHostAddress();

                    int port = serviceGroup.getVersion().isProxy() ? MainConfiguration.getInstance().getProxyStartPort() : MainConfiguration.getInstance().getSpigotStartPort();
                    while (isPortUsed(port)) {
                        port++;
                    }

                    CloudServer service = new SimpleCloudServer(serviceGroup.getName(), this.getPossibleServiceIDByGroup(serviceGroup), port, address);
                    CloudDriver.getInstance().getServiceManager().registerService(service);

                    if (thisSidesNode) {
                        CloudDriver.getInstance().getLogger().info("This Node queued §a" + service.getName() + " §8| §bPort " + service.getPort() + "§8| §bCapacity " + service.getMaxPlayers() + " §8| §bType " + (service.getConfiguration().getVersion().isProxy() ? "Proxy" : "Spigot") + " §8| §bState " + service.getServiceState().getName());
                    } else {
                        CloudDriver.getInstance().getLogger().info("Node '" + nodeClient.getName() + "' §8(§b" + nodeClient.getChannel() + "§8) §7queued §a" + service.getName() + " §8| §bPort " + service.getPort() + "§8| §bCapacity " + service.getMaxPlayers() + " §8| §bType " + (service.getConfiguration().getVersion().isProxy() ? "Proxy" : "Spigot") + " §8| §bState " + service.getServiceState().getName());
                    }
                });
    }

    private boolean minBootableServiceExists() {
        return this.getAmountOfBootableServices() >= MAX_BOOTABLE_SERVICES;
    }

    private int getAmountOfBootableServices() {
        return CloudDriver.getInstance().getServiceManager().getAllServicesByState(ServiceState.STARTING).size();
    }

    public int getAmountOfGroupServices(ServerConfiguration serviceGroup) {
        return (int) CloudDriver.getInstance().getServiceManager().getAllCachedServices().stream()
                .filter(it -> it.getConfiguration().equals(serviceGroup)).count();
    }

    private int getPossibleServiceIDByGroup(ServerConfiguration serviceGroup) {
        int id = 1;
        while (this.isServiceIDAlreadyExists(serviceGroup, id)) id++;
        return id;
    }

    private boolean isServiceIDAlreadyExists(ServerConfiguration serviceGroup, int id) {
        return CloudDriver.getInstance().getServiceManager().getAllServicesByGroup(serviceGroup).stream().anyMatch(it -> id == it.getServiceID());
    }

    private boolean isPortUsed(int port) {
        for (CloudServer service : NodeDriver.getInstance().getServiceManager().getAllCachedServices()) {
            if (service.getConfiguration().getNode().equals(NodeDriver.getInstance().getExecutor().getNodeName())) {
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
