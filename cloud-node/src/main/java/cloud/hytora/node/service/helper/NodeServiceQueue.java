package cloud.hytora.node.service.helper;

import cloud.hytora.driver.CloudDriver;


import cloud.hytora.driver.common.PublishingType;
import cloud.hytora.driver.entity.node.INode;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.entity.services.impl.UniversalCloudServer;
import cloud.hytora.driver.entity.services.task.ServiceTask;
import cloud.hytora.driver.entity.services.utils.ServiceState;
import cloud.hytora.driver.event.defaults.node.CloudEventNodeRegister;
import cloud.hytora.driver.event.defaults.server.CloudEventServiceRegistered;
import cloud.hytora.driver.event.listener.EventListener;
import cloud.hytora.driver.networking.packets.other.PacketServiceQueue;
import cloud.hytora.driver.networking.packets.response.NetworkResponseState;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.node.NodeDriver;
import cloud.hytora.driver.config.def.UniversalNetworkConfig;
import cloud.hytora.node.ServiceQueue;
import lombok.Getter;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

@Getter
public class NodeServiceQueue implements ServiceQueue, PacketHandler<PacketServiceQueue> {

    private final int maxBootableServices;

    private final Collection<String> pausedGroups;

    public NodeServiceQueue() {
        this.maxBootableServices = NodeDriver.getInstance().getNode().getConfig().getMaxBootableServicesAtSameTime();
        this.pausedGroups = new ArrayList<>();

        CloudDriver.getInstance().getExecutor().registerPacketHandler(this);
        CloudDriver.getInstance().getEventManager().registerListener(this);

        this.dequeue();
    }

    public void dequeue() {
        if (!NodeDriver.getInstance().isRunning() || System.getProperty("cloud.hytora.launcher.disableServerStart", "false").equals("true")) {
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
                                System.out.println(2);
                                CloudDriver.getInstance().getLogger().error("Tried to start {} but the Node(s) {} for Servers of Configuration {} is not connected!", cloudServer.getName(), cloudServer.getTask().getPossibleNodes(), cloudServer.getTask().getName());
                            })
                            .onTaskSucess(node -> {
                                if (!node.hasEnoughMemoryToStart(cloudServer)) {
                                    CloudDriver.getInstance().getLogger().warn("'{}' couldn't start {} because its maximum memory of {} has been reached!", node.getName(), cloudServer.getName(), node.getConfig().getMemory());
                                    return;
                                }
                                node.startServerAsync(cloudServer)
                                        .onTaskSucess(state -> {
                                            cloudServer.setServiceState(ServiceState.STARTING);
                                            cloudServer.update();
                                            dequeue();
                                        })
                                        .onTaskFailed(e -> {
                                            CloudDriver.getInstance().getLogger().error("Tried to start {} but the Node brought back following erorr: {}", cloudServer.getName(), e.getMessage() );
                                        });
                            });
                });

    }

    public void queue() {
        CloudDriver.getInstance().getServiceTaskManager().getAllCachedTasks().stream()
                .filter(task -> (
                        this.getAmountOfGroupServices(task) < task.getMinOnlineService()
                        ||this.getAmountOfGroupServices(task) < task.getMaxOnlineService()
                        ))
               // .filter(task -> this.getAmountOfGroupServices(task) < task.getMaxOnlineService())
                .filter(task -> pausedGroups.stream().noneMatch(s -> s.equalsIgnoreCase(task.getName())))
                .sorted(Comparator.comparingInt(ServiceTask::getStartOrder))
                .forEach(task -> {
                    INode node = task.findAnyNode();

                    if (node == null) {
                        CloudDriver.getInstance().getLogger().warn("§cTried to start a Service of Group '§e" + task.getName() + "§c' but no §eNode(s) §cwith name '§e" + task.getPossibleNodes() + "§c' is connected!");
                        return;
                    }

                    int port = task.getVersion().isProxy() ? UniversalNetworkConfig.getInstance().getProxyStartPort() : UniversalNetworkConfig.getInstance().getSpigotStartPort();
                    while (isPortUsed(port)) {
                        port++;
                    }
                    CloudService service = new UniversalCloudServer(task.getName(), this.getPossibleServiceIDByGroup(task), port, node.getConfig().getAddress().getHost());
                    service.setRunningNodeName(node.getName());
                    CloudDriver.getInstance().getServiceManager().registerService(service);
                    CloudDriver.getInstance().getEventManager().callEvent(new CloudEventServiceRegistered(service), PublishingType.GLOBAL);
                });
    }

    @Override
    public void addPausedGroup(String name) {
        this.pausedGroups.add(name);
    }

    @Override
    public void removePausedGroup(String name) {
        this.pausedGroups.removeIf(s -> s.equalsIgnoreCase(name));
    }

    private boolean minBootableServiceExists() {
        return this.getAmountOfBootableServices() >= maxBootableServices;
    }

    private int getAmountOfBootableServices() {
        return CloudDriver.getInstance().getServiceManager().getAllServicesByState(ServiceState.STARTING).size();
    }

    public int getAmountOfGroupServices(ServiceTask serviceGroup) {
        return (int) CloudDriver.getInstance().getServiceManager().getAllCachedServices().stream()
                .filter(it -> it.getTask().getName().equalsIgnoreCase(serviceGroup.getName())).count();
    }

    private int getPossibleServiceIDByGroup(ServiceTask serviceGroup) {
        int id = 1;
        while (this.isServiceIDAlreadyExists(serviceGroup, id)) id++;
        return id;
    }

    private boolean isServiceIDAlreadyExists(ServiceTask serviceGroup, int id) {
        return CloudDriver.getInstance().getServiceManager().getAllServicesByTask(serviceGroup).stream().anyMatch(it -> id == it.getServiceID());
    }

    private boolean isPortUsed(int port) {
        for (CloudService service : NodeDriver.getInstance().getServiceManager().getAllCachedServices()) {
            if (service.getTask().getPossibleNodes().stream().anyMatch(n -> n.equalsIgnoreCase(NodeDriver.getInstance().getExecutor().getName()))) {
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

    @EventListener
    public void handle(CloudEventNodeRegister event) {
        this.dequeue();
    }

    @Override
    public void handle(PacketChannel channel, PacketServiceQueue packet) {
        PacketBuffer buffer = packet.buffer();
        switch (buffer.readEnum(PacketServiceQueue.PayLoad.class)) {
            case QUEUE:
                this.queue();
                break;
            case DEQUEUE:
                this.dequeue();
                break;
            case SCP_ADD_GROUP:
                String group = buffer.readString();
                this.addPausedGroup(group);
                break;
            case SCP_REMOVE_GROUP:
                String rGroup = buffer.readString();
                this.removePausedGroup(rGroup);
                break;
            case SCP_GET_PAUSED_GROUPS:
                packet.sendResponse()
                        .setState(NetworkResponseState.OK)
                        .setBuffer(b -> b.writeStringCollection(this.pausedGroups))
                        .execute();
                break;
        }
    }
}
