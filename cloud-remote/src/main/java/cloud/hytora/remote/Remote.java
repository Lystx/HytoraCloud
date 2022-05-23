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
import cloud.hytora.driver.services.utils.ServiceIdentity;
import cloud.hytora.driver.storage.DriverStorage;
import cloud.hytora.driver.storage.RemoteDriverStorage;
import cloud.hytora.driver.networking.AdvancedNetworkExecutor;
import cloud.hytora.remote.impl.*;
import cloud.hytora.remote.impl.handler.RemoteCommandHandler;
import cloud.hytora.remote.impl.handler.RemoteLoggingHandler;
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
    private final ServiceIdentity property;

    public Remote(Logger logger) {
        super(logger, DriverEnvironment.SERVICE);

        instance = this;

        this.commandSender = new DefaultCommandSender("Wrapper", this.getConsole()).function(System.out::println);
        this.property = new ServiceIdentity().read(new File("property.json"));

        this.client = new RemoteNetworkClient(property.getService(), property.getHostname(), property.getPort(), DocumentFactory.emptyDocument());

        //registering handlers
        this.client.registerPacketHandler(new RemoteLoggingHandler());
        this.client.registerPacketHandler(new RemoteCommandHandler());

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
            cloudServer.update();
        }, 0, SERVER_PUBLISH_INTERVAL, TimeUnit.MILLISECONDS);
    }

    public static void main(String[] args) {
        try {

            HandledLogger logger = new HandledAsyncLogger(LogLevel.TRACE);
            logger.addHandler(new DefaultLogHandler());
            Logger.setFactory(logger);

            Remote remote = new Remote(logger);
            
            List<String> arguments = new ArrayList<>(Arrays.asList(args));
            Class<?> main = Class.forName(arguments.remove(0));
            Method method = main.getMethod("main", String[].class);
            Thread thread = new Thread(() -> {
                try {

                    CloudDriver.getInstance().getLogger().info("Waiting for CacheUpdate to start Application...");
                    CloudDriver.getInstance().getExecutor().registerSelfDestructivePacketHandler((PacketHandler<DriverUpdatePacket>) (wrapper1, packet) -> {
                        remote.getStorage().fetch();
                        CloudDriver.getInstance().getLogger().info("Launching '" + main.getName() + "' ...");
                        try {
                            method.invoke(null, (Object) arguments.toArray(new String[0]));
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                        CloudDriver.getInstance().getLogger().info("Launched '" + main.getName() + "'!");
                    });
                } catch (Exception exception) {
                    CloudDriver.getInstance().getLogger().info("Couldn't launch '" + main.getName() + "'!");
                    exception.printStackTrace();
                }
            }, "Minecraft-Thread");
            thread.setContextClassLoader(ClassLoader.getSystemClassLoader());
            thread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Remote getInstance() {
        return instance;
    }

    public CloudServer thisService() {
        return this.serviceManager.getAllCachedServices().stream().filter(it -> it.getName().equalsIgnoreCase(this.property.getService())).findAny().orElse(null);
    }

    @Override
    public void shutdown() {
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
