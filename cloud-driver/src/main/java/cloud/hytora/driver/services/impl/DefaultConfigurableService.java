package cloud.hytora.driver.services.impl;

import cloud.hytora.common.function.ExceptionallyBiConsumer;
import cloud.hytora.common.task.Task;
import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.DriverEnvironment;
import cloud.hytora.driver.event.DestructiveListener;
import cloud.hytora.driver.event.IEventManager;
import cloud.hytora.driver.event.defaults.server.ServiceReadyEvent;
import cloud.hytora.driver.networking.EndpointNetworkExecutor;
import cloud.hytora.driver.networking.cluster.ClusterClientExecutor;
import cloud.hytora.driver.services.ICloudServiceManager;
import cloud.hytora.driver.services.packet.ServiceConfigPacket;
import cloud.hytora.driver.node.INode;
import cloud.hytora.driver.node.INodeManager;
import cloud.hytora.driver.services.IFutureCloudServer;
import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.driver.services.task.IServiceTask;
import cloud.hytora.driver.services.template.ITemplate;
import cloud.hytora.driver.services.utils.version.ServiceVersion;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

public class DefaultConfigurableService implements IFutureCloudServer {

    private final IServiceTask serviceTask;

    private int port;
    private int memory;

    private int maxPlayers;

    private boolean ignoreOfLimit;

    private String motd;

    private UUID uniqueId;

    private String node;

    private Document properties;

    private Collection<ITemplate> templates;

    private ServiceVersion version;

    public DefaultConfigurableService(IServiceTask serviceTask) {
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
    public IFutureCloudServer port(int port) {
        this.port = port;
        return this;
    }

    @Override
    public IFutureCloudServer uniqueId(UUID uniqueId) {
        this.uniqueId = uniqueId;
        return this;
    }

    @Override
    public IFutureCloudServer memory(int memoryInMB) {
        this.memory = memoryInMB;
        return this;
    }

    @Override
    public IFutureCloudServer motd(String motd) {
        this.motd = motd;
        return this;
    }

    @Override
    public IFutureCloudServer properties(Document document) {
        this.properties = document;
        return this;
    }

    @Override
    public IFutureCloudServer maxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
        return this;
    }

    @Override
    public IFutureCloudServer node(String node) {
        this.node = node;
        return this;
    }

    @Override
    public IFutureCloudServer version(ServiceVersion version) {
        this.version = version;
        return this;
    }

    @Override
    public IFutureCloudServer templates(ITemplate... templates) {
        this.templates = Arrays.asList(templates);
        return this;
    }

    @Override
    public IFutureCloudServer ignoreIfLimitOfServicesReached() {
        this.ignoreOfLimit = true;
        return this;
    }


    @Override
    public Task<ICloudServer> start() {
        Task<ICloudServer> task = Task.empty();


        Task.runAsync(() -> {
            if (CloudDriver.getInstance().getEnvironment() == DriverEnvironment.NODE) {
                EndpointNetworkExecutor executor = (EndpointNetworkExecutor) CloudDriver.getInstance().getNetworkExecutor();

                ClusterClientExecutor nodeClient = executor.getClient(node).orElse(null);
                boolean thisSidesNode = serviceTask.getPossibleNodes().contains(CloudDriver.getInstance().getNetworkExecutor().getName());

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

                ICloudServer service = new UniversalCloudServer(serviceTask.getName(), newServiceId(), port, address);
                service.setProperties(properties);
                service.setMaxPlayers(maxPlayers);
                service.setUniqueId(uniqueId);
                service.setRunningNodeName(node);
                service.editPingProperties(ping -> {
                    ping.setMotd(motd);
                });

                CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudServiceManager.class).registerService(service);


                if (!CloudDriver.getInstance().isRunning()) {
                    return;
                }

                INodeManager nodeManager = CloudDriver.getInstance().getProviderRegistry().getUnchecked(INodeManager.class);
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

                CloudDriver.getInstance().getProviderRegistry().getUnchecked(IEventManager.class).registerDestructiveHandler(ServiceReadyEvent.class, (ExceptionallyBiConsumer<ServiceReadyEvent, DestructiveListener>) (event, listener) -> {

                    ICloudServer cloudServer = event.getCloudServer();
                    if (cloudServer.getUniqueId().equals(this.uniqueId)) {
                        task.setResult(cloudServer);
                        listener.destroy();
                    }
                });
                packet.publishAsync();
            }
        });

        return task;
    }

    private int newServiceId() {
        return (CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudServiceManager.class).getAllServicesByTask(serviceTask).size() + 1);
    }

    private boolean isPortUsed(int port) {
        for (ICloudServer service : CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudServiceManager.class).getAllCachedServices()) {
            if (service.getTask().getPossibleNodes().contains(CloudDriver.getInstance().getNetworkExecutor().getName()) && service.getPort() == port) {
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
