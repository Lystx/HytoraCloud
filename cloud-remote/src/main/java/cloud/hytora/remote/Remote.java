package cloud.hytora.remote;

import cloud.hytora.common.logging.Logger;
import cloud.hytora.common.logging.LogLevel;
import cloud.hytora.common.logging.handler.HandledAsyncLogger;
import cloud.hytora.common.logging.handler.HandledLogger;
import cloud.hytora.common.misc.StringUtils;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.DriverEnvironment;
import cloud.hytora.driver.command.CommandManager;
import cloud.hytora.driver.command.DefaultCommandSender;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.driver.command.Console;
import cloud.hytora.driver.InternalDriverEventAdapter;
import cloud.hytora.driver.event.defaults.driver.DriverLogEvent;
import cloud.hytora.driver.http.api.HttpServer;
import cloud.hytora.driver.message.ChannelMessenger;
import cloud.hytora.driver.module.ModuleManager;
import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.networking.packets.DriverLoggingPacket;
import cloud.hytora.driver.networking.packets.DriverUpdatePacket;
import cloud.hytora.driver.node.NodeManager;
import cloud.hytora.driver.player.PlayerManager;
import cloud.hytora.driver.services.CloudServer;
import cloud.hytora.driver.services.ServiceManager;
import cloud.hytora.driver.services.configuration.ConfigurationManager;
import cloud.hytora.driver.services.utils.RemoteIdentity;
import cloud.hytora.driver.storage.DriverStorage;
import cloud.hytora.driver.storage.RemoteDriverStorage;
import cloud.hytora.driver.networking.AdvancedNetworkExecutor;
import cloud.hytora.remote.adapter.RemoteAdapter;
import cloud.hytora.remote.adapter.proxy.RemoteProxyAdapter;
import cloud.hytora.remote.impl.*;
import cloud.hytora.remote.impl.handler.RemoteCacheUpdateHandler;
import cloud.hytora.remote.impl.handler.RemoteCommandHandler;
import cloud.hytora.remote.impl.handler.RemoteLoggingHandler;
import cloud.hytora.remote.impl.handler.RemoteNodeUpdateHandler;
import cloud.hytora.remote.impl.log.DefaultLogHandler;
import cloud.hytora.remote.impl.module.RemoteModuleManager;
import lombok.Getter;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Getter
public class Remote extends CloudDriver {

    private static Remote instance;
    private final ConfigurationManager configurationManager;
    private final ServiceManager serviceManager;
    private final PlayerManager playerManager;
    private final CommandManager commandManager;
    private final CommandSender commandSender;
    private final DriverStorage storage;
    private final ChannelMessenger channelMessenger;
    private final NodeManager nodeManager;
    private final ModuleManager moduleManager;

    @Setter
    private RemoteAdapter adapter;

    private final RemoteNetworkClient client;
    private final RemoteIdentity property;

    public Remote(RemoteIdentity identity, Logger logger, Runnable... ifConnectionFailed) {
        super(logger, DriverEnvironment.SERVICE);

        instance = this;

        this.commandSender = new DefaultCommandSender("Wrapper", this.getConsole()).function(System.out::println);
        this.property = identity;

        this.client = new RemoteNetworkClient(property.getAuthKey(), property.getName(), property.getHostname(), property.getPort(), DocumentFactory.emptyDocument(), ifConnectionFailed);

        //registering handlers
        this.client.registerPacketHandler(new RemoteLoggingHandler());
        this.client.registerPacketHandler(new RemoteCommandHandler());
        this.client.registerPacketHandler(new RemoteCacheUpdateHandler());
        this.client.registerPacketHandler(new RemoteNodeUpdateHandler());

        //registering event handlers
        new InternalDriverEventAdapter(this.eventManager, client);

        this.configurationManager = new RemoteConfigurationManager();
        this.serviceManager = new RemoteServiceManager(property);
        this.playerManager = new RemotePlayerManager(this.eventManager);
        this.commandManager = new RemoteCommandManager();
        this.channelMessenger = new RemoteChannelMessenger(this.client);
        this.nodeManager = new RemoteNodeManager();
        this.moduleManager = new RemoteModuleManager();

        this.storage = new RemoteDriverStorage(this.client);

        this.scheduledExecutor.scheduleAtFixedRate(() -> {
            CloudServer cloudServer = thisService();
            perform(cloudServer != null, cloudServer::update);
        }, 0, SERVER_PUBLISH_INTERVAL, TimeUnit.MILLISECONDS);
    }

    private static Logger init() {
        HandledLogger logger = new HandledAsyncLogger(LogLevel.TRACE);
        logger.addHandler(new DefaultLogHandler());
        logger.addHandler(entry -> CloudDriver.getInstance().getEventManager().callEvent(new DriverLogEvent(entry)));
        Logger.setFactory(logger);

        return logger;
    }

    public static Remote initFromOtherInstance(RemoteIdentity identity, Consumer<DriverUpdatePacket> thenExecute, Runnable... ifConnectionFailed) {
        Remote remote = new Remote(identity, init(), ifConnectionFailed);
        Thread thread = new Thread(() -> {
            try {

                CloudDriver.getInstance().getLogger().info("Waiting for CacheUpdate to start Application...");
                CloudDriver.getInstance().getExecutor().registerSelfDestructivePacketHandler((PacketHandler<DriverUpdatePacket>) (wrapper1, packet) -> {
                    CloudDriver.getInstance().getLogger().info("Received CacheUpdate!");
                    remote.getClient().getPacketChannel().sendPacket(new DriverLoggingPacket(NetworkComponent.of("Node-"), "Hello Test"));

                    thenExecute.accept(packet);
                    remote.getClient().getPacketChannel().sendPacket(new DriverLoggingPacket(NetworkComponent.of("Node-"), "Hello Test"));

                    RemoteProxyAdapter proxy = Remote.getInstance().getProxyAdapterOrNull();
                    if (proxy != null) {
                        CloudDriver.getInstance().getLogger().info("Pre registered all services");
                        proxy.clearServices();
                        for (CloudServer allCachedService : packet.getAllCachedServices()) {
                            proxy.registerService(allCachedService);
                        }
                    }
                    remote.getStorage().fetch(); //fetching storage data
                });
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }, "RemoteThread");
        thread.setContextClassLoader(ClassLoader.getSystemClassLoader());
        thread.start();

        return remote;
    }

    public static void main(String[] args) {
        try {

            List<String> arguments = new ArrayList<>(Arrays.asList(args));
            Class<?> main = Class.forName(arguments.remove(0));
            Method method = main.getMethod("main", String[].class);
            initFromOtherInstance(new RemoteIdentity().read(new File("property.json")), packet -> {
                CloudDriver.getInstance().getLogger().info("Launching '" + main.getName() + "' ...");
                try {
                    method.invoke(null, (Object) arguments.toArray(new String[0]));
                    CloudDriver.getInstance().getLogger().info("Launched '" + main.getName() + "'!");
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                    CloudDriver.getInstance().getLogger().error("Couldn't launch '" + main.getName() + "'!");
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Remote getInstance() {
        return instance;
    }

    public RemoteProxyAdapter getProxyAdapter() {
        return perform(adapter instanceof RemoteProxyAdapter, () -> cast(adapter), new IllegalStateException("Not a " + RemoteProxyAdapter.class.getSimpleName()));
    }

    public RemoteProxyAdapter getProxyAdapterOrNull() {
        return perform(adapter instanceof RemoteProxyAdapter, () -> cast(adapter), (Supplier<RemoteProxyAdapter>) () -> null);
    }

    public CloudServer thisService() {
        return this.serviceManager.getAllCachedServices().stream().filter(it -> it.getName().equalsIgnoreCase(this.property.getName())).findAny().orElse(null);
    }

    @Override
    public void shutdown() {
        if (adapter != null) {

        }
        // TODO: 11.04.2022
    }

    @Override
    public void logToExecutor(NetworkComponent component, String message, Object... args) {
        message = StringUtils.formatMessage(message, args);
        DriverLoggingPacket packet = new DriverLoggingPacket(component, message);
        this.client.sendPacket(packet);
    }

    @NotNull
    @Override
    public AdvancedNetworkExecutor getExecutor() {
        return client;
    }

    @Nullable
    @Override
    public HttpServer getHttpServer() {
        return null;
    }

    @Nullable
    @Override
    public Console getConsole() {
        return null;
    }

}