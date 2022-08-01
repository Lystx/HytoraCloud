package cloud.hytora.driver.services.impl;

import cloud.hytora.common.function.ExceptionallyBiConsumer;
import cloud.hytora.common.task.Task;
import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.DriverEnvironment;
import cloud.hytora.driver.event.DestructiveListener;
import cloud.hytora.driver.event.defaults.server.ServiceReadyEvent;
import cloud.hytora.driver.networking.EndpointNetworkExecutor;
import cloud.hytora.driver.networking.cluster.ClusterClientExecutor;
import cloud.hytora.driver.networking.packets.services.ServiceConfigPacket;
import cloud.hytora.driver.node.INode;
import cloud.hytora.driver.node.NodeManager;
import cloud.hytora.driver.services.ConfigurableService;
import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.driver.services.task.ServiceTask;
import cloud.hytora.driver.services.template.ServiceTemplate;
import cloud.hytora.driver.services.utils.version.ServiceVersion;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

public class DefaultConfigurableService implements ConfigurableService {

    private final ServiceTask serviceTask;

    private int port;
    private int memory;

    private int maxPlayers;

    private boolean ignoreOfLimit;

    private String motd;

    private UUID uniqueId;

    private String node;

    private Document properties;

    private Collection<ServiceTemplate> templates;

    private ServiceVersion version;

    public DefaultConfigurableService(ServiceTask serviceTask) {
        this.serviceTask = serviceTask;

        this.port = -1;
        this.memory = serviceTask.getMemory();
        this.motd = serviceTask.getMotd();
        this.node = serviceTask.getPossibleNodes().stream().findAny().get();
        this.templates = serviceTask.getTemplates();
        this.maxPlayers = serviceTask.getDefaultMaxPlayers();
        this.properties = DocumentFactory.newJsonDocument();
        this.version = serviceTask.getVersion();
        this.ignoreOfLimit = false;
        this.uniqueId = UUID.randomUUID();
    }

    @Override
    public ConfigurableService port(int port) {
        this.port = port;
        return this;
    }

    @Override
    public ConfigurableService uniqueId(UUID uniqueId) {
        this.uniqueId = uniqueId;
        return this;
    }

    @Override
    public ConfigurableService memory(int memoryInMB) {
        this.memory = memoryInMB;
        return this;
    }

    @Override
    public ConfigurableService motd(String motd) {
        this.motd = motd;
        return this;
    }

    @Override
    public ConfigurableService properties(Document document) {
        this.properties = document;
        return this;
    }

    @Override
    public ConfigurableService maxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
        return this;
    }

    @Override
    public ConfigurableService node(String node) {
        this.node = node;
        return this;
    }

    @Override
    public ConfigurableService version(ServiceVersion version) {
        this.version = version;
        return this;
    }

    @Override
    public ConfigurableService templates(ServiceTemplate... templates) {
        this.templates = Arrays.asList(templates);
        return this;
    }

    @Override
    public ConfigurableService ignoreIfLimitOfServicesReached() {
        this.ignoreOfLimit = true;
        return this;
    }


    @Override
    public Task<ICloudServer> start() {
        Task<ICloudServer> task = Task.empty();


        Task.runAsync(() -> {
            if (CloudDriver.getInstance().getEnvironment() == DriverEnvironment.NODE) {
                EndpointNetworkExecutor executor = (EndpointNetworkExecutor) CloudDriver.getInstance().getExecutor();

                ClusterClientExecutor nodeClient = executor.getClient(node).orElse(null);
                boolean thisSidesNode = serviceTask.getPossibleNodes().contains(CloudDriver.getInstance().getExecutor().getName());

                if (nodeClient == null && !thisSidesNode) {
                    CloudDriver.getInstance().getLogger().info("Tried to start a Service of Task '" + serviceTask.getName() + "' but no Node with name '" + node + "' is connected!");
                    return;
                }

                String address = thisSidesNode ? "127.0.0.1" : ((InetSocketAddress) nodeClient.getChannel().remoteAddress()).getAddress().getHostAddress();

                if (port == -1) {
                    port = serviceTask.getVersion().isProxy() ? executor.getProxyStartPort() : executor.getSpigotStartPort();
                    while (isPortUsed(port)) {
                        port++;
                    }
                }

                ICloudServer service = new DriverServiceObject(serviceTask.getName(), newServiceId(), port, address);
                service.setProperties(properties);
                service.setMaxPlayers(maxPlayers);
                service.setUniqueId(uniqueId);
                service.setRunningNodeName(node);
                service.setMotd(motd);

                CloudDriver.getInstance().getServiceManager().registerService(service);


                if (!CloudDriver.getInstance().isRunning()) {
                    return;
                }

                NodeManager nodeManager = CloudDriver.getInstance().getNodeManager();
                Task<INode> node = nodeManager.getNode(this.node);

                node.ifPresent(n -> n.startServer(service));
                node.ifEmpty(n -> CloudDriver.getInstance().getLogger().error("Tried to start {} but the Node {} for Servers of Configuration {} is not connected!", service.getName(), this.node, serviceTask.getName()));

            } else {

                ServiceConfigPacket packet = new ServiceConfigPacket(
                        uniqueId,
                        serviceTask.getName(),
                        port,
                        memory,
                        maxPlayers,
                        ignoreOfLimit,
                        motd,
                        node,
                        properties,
                        templates,
                        version
                );

                CloudDriver.getInstance().getEventManager().registerDestructiveHandler(ServiceReadyEvent.class, (ExceptionallyBiConsumer<ServiceReadyEvent, DestructiveListener>) (event, listener) -> {

                    ICloudServer ICloudServer = event.getICloudServer();
                    System.out.println("eVENT -> " + this.uniqueId + " / " + ICloudServer.getUniqueId());
                    if (ICloudServer.getUniqueId().equals(this.uniqueId)) {
                        task.setResult(ICloudServer);
                        System.out.println("DEBUG " + ICloudServer);
                        listener.destroy();
                    }
                });
                packet.publishAsync();
            }
        });

        return task;
    }

    private int newServiceId() {
        return (CloudDriver.getInstance().getServiceManager().getAllServicesByTask(serviceTask).size() + 1);
    }

    private boolean isPortUsed(int port) {
        for (ICloudServer service : CloudDriver.getInstance().getServiceManager().getAllCachedServices()) {
            if (service.getTask().getPossibleNodes().equals(CloudDriver.getInstance().getExecutor().getName())) {
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
