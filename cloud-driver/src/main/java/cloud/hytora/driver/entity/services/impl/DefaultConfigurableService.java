package cloud.hytora.driver.entity.services.impl;

import cloud.hytora.common.function.ExceptionallyBiConsumer;
import cloud.hytora.common.task.Task;
import cloud.hytora.document.Document;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.PublishingType;
import cloud.hytora.driver.config.INetworkConfig;
import cloud.hytora.driver.entity.node.INode;
import cloud.hytora.driver.entity.node.NodeManager;
import cloud.hytora.driver.entity.services.ConfigurableService;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.entity.services.task.ServiceTask;
import cloud.hytora.driver.entity.services.template.ServiceTemplate;
import cloud.hytora.driver.entity.services.utils.version.ServiceVersion;
import cloud.hytora.driver.event.defaults.server.CloudEventServiceRegistered;
import cloud.hytora.driver.event.listener.DestructiveListener;
import cloud.hytora.driver.event.defaults.server.CloudEventServiceReady;
import cloud.hytora.driver.networking.EndpointNetworkExecutor;
import cloud.hytora.driver.networking.packets.entities.PacketCloudEntityService;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

public class DefaultConfigurableService implements ConfigurableService {

    private final ServiceTask serviceTask;

    private int port;
    private int memory;
    private int timeOutIfNoPlayers = -1;

    private int maxPlayers;

    private boolean ignoreOfLimit;

    private String motd;

    private UUID uniqueId;

    private String node;
    private String worldPath;

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
        this.properties = Document.gson();
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
    public ConfigurableService timeOutIfNoPlayers(int i) {
        this.timeOutIfNoPlayers = i;
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
    public ConfigurableService defaultWorld(ServiceTemplate template, String filePath) {

        File templateDir = new File(CloudDriver.Constants.TEMPLATES_DIR, template.buildTemplatePath());
        File destination = new File(templateDir, filePath);

        this.worldPath = destination.getPath();
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
    public Task<CloudService> start() {
        Task<CloudService> task = Task.empty();


        Task.runAsync(() -> {
            if (CloudDriver.getInstance().getEnvironment() == CloudDriver.Environment.NODE) {
                EndpointNetworkExecutor executor = (EndpointNetworkExecutor) CloudDriver.getInstance().getExecutor();

                PacketChannel nodeChannel = executor.getConnectedChannel(node);


                boolean thisSidesNode = serviceTask.getPossibleNodes().contains(CloudDriver.getInstance().getExecutor().getName());

                if (nodeChannel == null && !thisSidesNode) {
                    CloudDriver.getInstance().getLogger().info("Tried to start a ConfigurableService of Task '" + serviceTask.getName() + "' but no Node with name '" + node + "' is connected!");
                    return;
                }

                String address = thisSidesNode ? "127.0.0.1" : ((InetSocketAddress) nodeChannel.context().channel().remoteAddress()).getAddress().getHostAddress();

                INetworkConfig config = CloudDriver.getInstance().getConfigManager().getConfig();

                if (port == -1) {
                    port = serviceTask.getVersion().isProxy() ? config.getProxyStartPort() : config.getSpigotStartPort();
                    while (isPortUsed(port)) {
                        port++;
                    }
                }

                CloudService service = new UniversalCloudServer(serviceTask.getName(), newServiceId(), port, address);
                service.setProperties(properties);
                service.setMaxPlayers(maxPlayers);
                service.setReady(false);
                service.setUniqueId(uniqueId);
                service.setRunningNodeName(node);
                service.setDefaultWorld(worldPath == null ? "world" : worldPath);
                service.setMotd(motd);


                if (timeOutIfNoPlayers != -1) {
                    service.setProperty("timeOutIfNoPlayers", timeOutIfNoPlayers);
                }

                CloudDriver.getInstance().getServiceManager().registerService(service);
                CloudDriver.getInstance().getEventManager().callEvent(new CloudEventServiceRegistered(service), PublishingType.GLOBAL);


                if (!CloudDriver.getInstance().isRunning()) {
                    return;
                }

                NodeManager nodeManager = CloudDriver.getInstance().getNodeManager();
                Task<INode> node = nodeManager.getNode(this.node);

                node.ifPresent(n -> n.startServer(service));
                node.ifEmpty(n -> CloudDriver.getInstance().getLogger().error("Tried to start {} but the Node {} for Servers of Configuration {} is not connected!", service.getName(), this.node, serviceTask.getName()));

            } else {


                PacketCloudEntityService packet = PacketCloudEntityService.forConfiguration(
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

                CloudDriver.getInstance().getEventManager().registerDestructiveHandler(CloudEventServiceReady.class, (ExceptionallyBiConsumer<CloudEventServiceReady, DestructiveListener>) (event, listener) -> {

                    CloudService cloudServer = event.getCloudServer();
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
        return (CloudDriver.getInstance().getServiceManager().getAllServicesByTask(serviceTask).size() + 1);
    }

    private boolean isPortUsed(int port) {
        for (CloudService service : CloudDriver.getInstance().getServiceManager().getAllCachedServices()) {
            if (service.getTask().getPossibleNodes().contains(CloudDriver.getInstance().getExecutor().getName()) && service.getPort() == port) {
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
