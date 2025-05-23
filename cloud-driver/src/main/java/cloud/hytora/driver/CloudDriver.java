package cloud.hytora.driver;

import cloud.hytora.common.collection.NamedThreadFactory;
import cloud.hytora.common.logging.Logger;
import cloud.hytora.context.IApplicationContext;
import cloud.hytora.driver.command.CommandManager;
import cloud.hytora.driver.config.INetworkConfig;
import cloud.hytora.driver.event.EventManager;
import cloud.hytora.driver.event.defaults.DefaultEventManager;
import cloud.hytora.driver.http.api.HttpRequest;
import cloud.hytora.driver.http.api.HttpServer;
import cloud.hytora.driver.message.ChannelMessage;
import cloud.hytora.driver.message.ChannelMessenger;
import cloud.hytora.driver.module.IModule;
import cloud.hytora.driver.module.ModuleManager;
import cloud.hytora.driver.networking.AdvancedNetworkExecutor;
import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import cloud.hytora.driver.node.INode;
import cloud.hytora.driver.node.NodeManager;
import cloud.hytora.driver.player.*;
import cloud.hytora.common.scheduler.Scheduler;
import cloud.hytora.driver.player.impl.DefaultFullJoinExecutor;
import cloud.hytora.driver.provider.ProviderRegistry;
import cloud.hytora.driver.provider.defaults.DefaultProviderRegistry;
import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.driver.services.ServiceManager;
import cloud.hytora.driver.services.task.IServiceTask;
import cloud.hytora.driver.services.task.ServiceTaskManager;
import cloud.hytora.driver.services.task.bundle.TaskGroup;
import cloud.hytora.driver.services.template.TemplateManager;
import cloud.hytora.driver.services.template.def.DefaultTemplateManager;
import cloud.hytora.driver.storage.DriverStorage;
import cloud.hytora.driver.command.sender.CommandSender;

import cloud.hytora.driver.networking.PacketProvider;
import cloud.hytora.driver.tps.TickWorker;
import cloud.hytora.driver.tps.def.DefaultTickWorker;

import io.netty.util.ResourceLeakDetector;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.JdkLoggerFactory;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


/**
 * The <b>CloudDriver</b> is the core of the API of HytoraCloud.
 * It allows the System internally and developers to make use of every Manager across the Network
 * For example you can get information about a specific {@link ICloudPlayer}, a specific {@link ICloudService},
 * a specific {@link IServiceTask}, a specific {@link TaskGroup}. <br>
 * Or you could manage the {@link HttpServer} and create {@link HttpRequest} as you'd like to. <br>
 * Or you could manage all the different connected {@link INode}s and tell them to start or stop a certain Server
 * <br><br>
 * So you see a <b>CloudDriver</b> is the key to everything Code-Related that you wanna do concerning HytoraCloud
 * <br><br>
 *
 * @author Lystx
 * @see #shutdown()
 * @since SNAPSHOT-1.0
 */
@Getter
public abstract class CloudDriver extends DefaultProviderRegistry {

    /**
     * The static instance of this Driver
     */
    @Getter
    private static CloudDriver instance;

    /**
     * The current driver environment
     */
    protected final Environment environment;

    /**
     * The default logger service
     */
    protected final Logger logger;

    /**
     * The default event manager
     */
    protected final EventManager eventManager;

    /**
     * The default template manager
     */
    protected final TemplateManager templateManager;

    /**
     * The java executor service
     */
    protected final ScheduledExecutorService scheduledExecutor;

    /**
     * The tps manager
     */
    protected final TickWorker tickWorker;

    /**
     * The cloud provided scheduler api
     */
    protected final Scheduler scheduler;

    /**
     * If the current driver instance is running
     */
    @Setter
    protected boolean running;

    /**
     * Constructs a new {@link CloudDriver} instance with a provided {@link Logger} instance <br>
     * and a provided {@link Environment} to declare the environment this Instance runs on
     * Then setting default instances for Interfaces like {@link EventManager} or {@link Scheduler}
     * and finally registering all {@link AbstractPacket}s
     * <br><br>
     *
     * @param logger      the logger instance
     * @param environment the environment
     */
    public CloudDriver(Logger logger, Environment environment) {
        super(true, (new DefaultEventManager()));
        instance = this;

        this.eventManager = this.manager; //from Super method is set
        this.environment = environment;
        this.logger = logger;
        this.templateManager = new DefaultTemplateManager();
        this.tickWorker = new DefaultTickWorker(20);
        this.scheduler = Scheduler.runTimeScheduler();
        this.scheduledExecutor = Executors.newScheduledThreadPool(4, new NamedThreadFactory("Scheduler"));

        // use jdk logger to prevent issues with older slf4j versions
        // like them bundled in spigot 1.8
        try {
            JdkLoggerFactory.class.getDeclaredField("INSTANCE");
            InternalLoggerFactory.setDefaultFactory(JdkLoggerFactory.INSTANCE);
        } catch (NoSuchFieldException e) {
            this.logger.error("Couldn't override Netty Logger to prevent slf4j-spam!");
        }

        //make sure its set
        Logger.setFactory(logger);

        // check if the leak detection level is set before overriding it
        // may be useful for debugging of the network
        if (System.getProperty("io.netty.leakDetection.level") == null) {
            ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.DISABLED);
        }

        //registering all packets
        PacketProvider.registerPackets();

        this.running = true; //set running state

        //setting first provider
        this.setProvider(PlayerFullJoinExecutor.class, new DefaultFullJoinExecutor());
    }

    public ProviderRegistry getProviderRegistry() {
        return this;
    }

    /**
     * Shuts down the current Driver Instance
     * (Node, Remote or whatever extends this class)
     */
    public abstract void shutdown();

    /**
     * Sends a message to a specific {@link NetworkComponent} using Packets <br>
     * This method <b>does not</b> log to the current Driver Instance itsself but only to
     * the provided {@link NetworkComponent}
     * <br> <br>
     * Example Usage: logToExecutor(component, "Hello User '{}' with age {}!", "Name", 23)
     * <br> <br>
     *
     * @param component the component to send a message to
     * @param message   the message to send (use {} to replace arguments)
     * @param args      the arguments to replace in the message
     */
    public abstract void logToExecutor(NetworkComponent component, String message, Object... args);

    /**
     * The current {@link DriverStorage} instance where
     * you can store every type of data you want
     * @see DriverStorage
     */
    @Nonnull
    public abstract DriverStorage getStorage();


    /**
     * Returns the config of the whole network
     */
    public abstract INetworkConfig getNetworkConfig();

    /**
     * The current {@link CommandSender} instance where
     * @see CommandSender
     */
    @Nonnull
    public abstract CommandSender getCommandSender();

    /**
     * The current {@link NodeManager} instance where
     * you can manage every {@link INode}
     * @see NodeManager
     */
    @Nonnull
    public abstract NodeManager getNodeManager();

    /**
     * The current {@link ChannelMessenger} instance where
     * you can send and receive {@link ChannelMessage}s
     * @see ChannelMessenger
     */
    @Nonnull
    public abstract ChannelMessenger getChannelMessenger();

    /**
     * The current {@link CommandManager} instance where
     * you manage every registered Command
     * @see CommandManager
     */
    @Nonnull
    public abstract CommandManager getCommandManager();

    /**
     * The current {@link PlayerManager} instance where
     * you can manage every {@link ICloudPlayer} and {@link CloudOfflinePlayer}
     * @see PlayerManager
     */
    @Nonnull
    public abstract PlayerManager getPlayerManager();

    /**
     * The current {@link ServiceManager} instance where
     * you can manage every {@link ICloudService}
     * @see ServiceManager
     */
    @Nonnull
    public abstract ServiceManager getServiceManager();

    /**
     * The current {@link ModuleManager} instance where
     * you can manage every {@link IModule}
     * @see ModuleManager
     */
    @Nonnull
    public abstract ModuleManager getModuleManager();

    /**
     * The current {@link ServiceTaskManager} instance where
     * you can manage every {@link IServiceTask} and {@link TaskGroup}
     * @see ServiceTaskManager
     */
    @Nonnull
    public abstract ServiceTaskManager getServiceTaskManager();

    /**
     * Returns the current {@link IApplicationContext} if this
     * programm is running on a Remot-Side
     */
    public abstract IApplicationContext getApplicationContext();

    /**
     * The current {@link AdvancedNetworkExecutor} instance
     * @see AdvancedNetworkExecutor
     */
    public abstract AdvancedNetworkExecutor getExecutor();


    /**
     * The {@link Environment} defines the Environment <br>
     * that a <b>{@link CloudDriver}</b> runs on
     * <br><br>
     * @author  Lystx
     * @since   SNAPSHOT-1.0
     */
    public enum Environment {

        /**
         * Should not be used
         */
        UNKNOWN,

        /**
         * The environment is a Node
         */
        NODE,

        /**
         * The Environment is a Remote
         */
        SERVICE

    }
}

