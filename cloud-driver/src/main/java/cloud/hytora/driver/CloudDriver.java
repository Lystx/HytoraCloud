package cloud.hytora.driver;

import cloud.hytora.common.DriverUtility;
import cloud.hytora.common.logging.Logger;
import cloud.hytora.common.scheduler.def.DefaultScheduler;
import cloud.hytora.context.IApplicationContext;
import cloud.hytora.driver.common.IClusterObject;
import cloud.hytora.driver.event.IEventManager;
import cloud.hytora.driver.event.defaults.DefaultEventManager;
import cloud.hytora.driver.http.api.HttpRequest;
import cloud.hytora.driver.http.api.HttpServer;
import cloud.hytora.driver.networking.IHandlerNetworkExecutor;
import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import cloud.hytora.driver.node.INode;
import cloud.hytora.driver.player.*;
import cloud.hytora.common.scheduler.Scheduler;
import cloud.hytora.driver.provider.ProviderRegistry;
import cloud.hytora.driver.provider.defaults.DefaultProviderRegistry;
import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.driver.services.task.IServiceTask;
import cloud.hytora.driver.services.task.bundle.TaskGroup;
import cloud.hytora.driver.services.template.ITemplateManager;
import cloud.hytora.driver.services.template.def.DefaultTemplateManager;

import cloud.hytora.driver.networking.PacketProvider;
import cloud.hytora.driver.tps.ICloudTickWorker;
import cloud.hytora.driver.tps.def.DefaultTickWorker;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.JdkLoggerFactory;
import lombok.Getter;
import lombok.Setter;


/**
 * The <b>CloudDriver</b> is the core of the API of HytoraCloud.
 * It allows the System internally and developers to make use of every Manager across the Network
 * For example you can get information about a specific {@link ICloudPlayer}, a specific {@link ICloudServer},
 * a specific {@link IServiceTask}, a specific {@link TaskGroup}. <br>
 * Or you could manage the {@link HttpServer} and create {@link HttpRequest} as you'd like to. <br>
 * Or you could manage all the different connected {@link INode}s and tell them to start or stop a certain Server
 * <br><br>
 * So you see a <b>CloudDriver</b> is the key to everything Code-Related that you want to do concerning HytoraCloud
 * <br><br>
 *
 * @author Lystx
 * @see #shutdown()
 * @since SNAPSHOT-1.0
 */
@Getter
public abstract class CloudDriver<T extends IClusterObject<T>> extends DriverUtility {

    /**
     * The interval that services take to publish their data to the cluster
     * (here: every 1.5 minutes)
     */
    public static final int SERVER_PUBLISH_INTERVAL = 90_000;

    /**
     * The max lost cycles of a server before it is declared timed out
     * (here: 3 minutes)
     */
    public static final int SERVER_MAX_LOST_CYCLES = 2;

    /**
     * The interval that nodes take to publish their data to the cluster
     * (here: every 5 seconds)
     */
    public static final int NODE_PUBLISH_INTERVAL = 5_000;

    /**
     * The max lost cycles of a node before it is declared timed out
     * (here: 25 seconds)
     */
    public static final int NODE_MAX_LOST_CYCLES = 5;

    /**
     * The public name for the Dashboard to be identified
     */
    public static final String APPLICATION_NAME = "Application";


    /**
     * The static instance of this Driver
     */
    @Getter
    private static CloudDriver<?> instance;

    /**
     * The current environment that defines this whole driver side
     * It is important to check on which side a method or command or something
     * else is being executed and to check which environment it is on you can use
     * the lombok-generated getter-method
     */
    protected final DriverEnvironment environment;

    /**
     * The current logger instance that helps you to log all of your output
     * If you want to display a colored message or debug something it is very useful
     * You can also use the lombok-generated getter-method
     */
    protected final Logger logger;

    /**
     * The current ProviderRegistry instance.
     * Very important to access all the managers you'd like to access
     *
     * @see ProviderRegistry for further information
     */
    protected final ProviderRegistry providerRegistry;

    /**
     * Variable to check if the current driver instance
     * is still running or if it has been terminated already
     *
     * There is a lombok-generated setter-method for internal use
     * Only use if you know what you are doing!
     */
    @Setter
    protected boolean running;

    /**
     * Constructs a new {@link CloudDriver} instance with a provided {@link Logger} instance <br>
     * and a provided {@link DriverEnvironment} to declare the environment this Instance runs on
     * Then setting default instances for Interfaces like {@link IEventManager} or {@link Scheduler}
     * and finally registering all {@link AbstractPacket}s
     * <br><br>
     *
     * @param logger      the logger instance
     * @param environment the environment
     */
    public CloudDriver(Logger logger, DriverEnvironment environment) {
        instance = this; //setting instance to constructed driver

        this.running = true;
        this.environment = environment;
        this.logger = logger;

        //registering default providers
        IEventManager eventManager = new DefaultEventManager();
        this.providerRegistry = new DefaultProviderRegistry(true, eventManager);
        this.providerRegistry.setProvider(PlayerFullJoinExecutor.class, new DefaultFullJoinExecutor());
        this.providerRegistry.setProvider(IEventManager.class, eventManager);
        this.providerRegistry.setProvider(ICloudTickWorker.class, new DefaultTickWorker(20));
        this.providerRegistry.setProvider(Scheduler.class, new DefaultScheduler());
        this.providerRegistry.setProvider(ITemplateManager.class, new DefaultTemplateManager());

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

        //registering packets for current instance
        PacketProvider.registerPackets();
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
     * Sends a message to the provided {@link NetworkComponent} and also logs this message<br>
     * to the <b>current Driver Instance</b>
     *
     * @param component the component to send a message to
     * @param message   the message to send (use {} to replace arguments)
     * @param args      the arguments to replace in the message
     * @see CloudDriver#logToExecutor(NetworkComponent, String, Object...) for more information
     */
    public void logToExecutorAndSelf(NetworkComponent component, String message, Object... args) {
        this.logToExecutor(component, message, args);
        this.logger.info(message, args);
    }

    /**
     * Returns the current {@link IHandlerNetworkExecutor} that is an extension
     * of the normal {@link cloud.hytora.driver.networking.INetworkExecutor}<br>
     * With the executor you can send Packets or listen to incoming packets etc.<br>
     * You can also create and send queries to await for important responses!
     *
     * @return network instance
     */
    public abstract IHandlerNetworkExecutor getNetworkExecutor();

    /**
     * Returns the current {@link IClusterObject} of this sides participant<br>
     * For example if you call this method on a Node-Side it will return the current Node,<br>
     * if you call it on the Service-Side it will return the current Service
     *
     * @return cluster participant
     */
    public abstract T thisSidesClusterParticipant();

}