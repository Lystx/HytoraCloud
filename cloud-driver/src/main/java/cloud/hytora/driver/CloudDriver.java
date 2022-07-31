package cloud.hytora.driver;

import cloud.hytora.common.DriverUtility;
import cloud.hytora.common.collection.NamedThreadFactory;
import cloud.hytora.common.logging.Logger;
import cloud.hytora.driver.command.CommandManager;
import cloud.hytora.driver.common.IClusterObject;
import cloud.hytora.driver.event.EventManager;
import cloud.hytora.driver.event.defaults.DefaultEventManager;
import cloud.hytora.driver.http.api.HttpRequest;
import cloud.hytora.driver.http.api.HttpServer;
import cloud.hytora.driver.message.ChannelMessage;
import cloud.hytora.driver.message.ChannelMessenger;
import cloud.hytora.driver.module.Module;
import cloud.hytora.driver.module.ModuleManager;
import cloud.hytora.driver.networking.AdvancedNetworkExecutor;
import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import cloud.hytora.driver.node.Node;
import cloud.hytora.driver.node.NodeManager;
import cloud.hytora.driver.player.CloudOfflinePlayer;
import cloud.hytora.driver.player.CloudPlayer;
import cloud.hytora.driver.player.PlayerManager;
import cloud.hytora.common.scheduler.Scheduler;
import cloud.hytora.driver.provider.ProviderRegistry;
import cloud.hytora.driver.provider.defaults.DefaultProviderRegistry;
import cloud.hytora.driver.services.ServiceInfo;
import cloud.hytora.driver.services.ServiceManager;
import cloud.hytora.driver.services.task.ServiceTask;
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
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;


/**
 * The <b>CloudDriver</b> is the core of the API of HytoraCloud.
 * It allows the System internally and developers to make use of every Manager across the Network
 * For example you can get information about a specific {@link CloudPlayer}, a specific {@link ServiceInfo},
 * a specific {@link ServiceTask}, a specific {@link TaskGroup}. <br>
 * Or you could manage the {@link HttpServer} and create {@link HttpRequest} as you'd like to. <br>
 * Or you could manage all the different connected {@link Node}s and tell them to start or stop a certain Server
 * <br><br>
 * So you see a <b>CloudDriver</b> is the key to everything Code-Related that you wanna do concerning HytoraCloud
 * <br><br>
 *
 * @author Lystx
 * @see #shutdown()
 * @since SNAPSHOT-1.0
 */
@Getter
@DriverStatus(version = "SNAPSHOT-1.3", experimental = true, developers = {"Lystx"})
public abstract class CloudDriver<T extends IClusterObject<T>> extends DriverUtility {

    /**
     * The static instance of this Driver
     */
    @Getter
    private static CloudDriver instance;

    /**
     * The current driver environment
     */
    protected final DriverEnvironment environment;

    /**
     * The default logger service
     */
    protected final Logger logger;

    /**
     * The provider registry to register/get providers
     */
    protected final ProviderRegistry providerRegistry;

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
     * The public name for the Dashboard to be identified
     */
    public static final String APPLICATION_NAME = "Application";

    /**
     * Constructs a new {@link CloudDriver} instance with a provided {@link Logger} instance <br>
     * and a provided {@link DriverEnvironment} to declare the environment this Instance runs on
     * Then setting default instances for Interfaces like {@link EventManager} or {@link Scheduler}
     * and finally registering all {@link AbstractPacket}s
     * <br><br>
     *
     * @param logger      the logger instance
     * @param environment the environment
     */
    public CloudDriver(Logger logger, DriverEnvironment environment) {
        instance = this;

        this.environment = environment;
        this.logger = logger;
        this.eventManager = new DefaultEventManager(); //eventManager needs to come before Registry bc it is needed in Registry
        this.providerRegistry = new DefaultProviderRegistry(true);
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
        PacketProvider.registerPackets();

        this.running = true;
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
     * <br> <br>
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
     * Public Method that tries to execute a given {@link Runnable} if a provided {@link Supplier} returns {@code true} <br>
     * or until the provided timeout in milliseconds has expired from the start of the operation
     * <br> <br>
     *
     * @param runnable the runnable to execute
     * @param request  the condition that has to be true
     * @param timeOut  the timeOut for this request in milliseconds
     */
    public void executeIf(Runnable runnable, Supplier<Boolean> request, long timeOut) {
        this.scheduledExecutor.execute(() -> {
            long deadline = System.currentTimeMillis() + timeOut;
            boolean done;

            do {
                done = request.get();
                if (!done) {
                    long msRemaining = deadline - System.currentTimeMillis();
                    if (msRemaining < 0) {
                        done = true;
                    }
                } else {
                    runnable.run();
                }
            } while (!done);
        });
    }

    /**
     * Executes a given {@link Runnable} if a provided {@link Supplier} returns {@code true} <br>
     * with a default timeout of <b>1 DAY</b>
     * <br> <br>
     *
     * @param runnable the runnable to execute
     * @param request  the condition that has to be true
     * @see CloudDriver#executeIf(Runnable, Supplier, long)
     */
    public void executeIf(Runnable runnable, Supplier<Boolean> request) {
        this.executeIf(runnable, request, TimeUnit.DAYS.toMillis(1));
    }

    /**
     * The current {@link DriverStorage} instance where
     * you can store every type of data you want
     * @see DriverStorage
     */
    @Nonnull
    public abstract DriverStorage getStorage();

    /**
     * The current {@link CommandSender} instance where
     * @see CommandSender
     */
    @Nonnull
    public abstract CommandSender getCommandSender();

    /**
     * The current {@link NodeManager} instance where
     * you can manage every {@link Node}
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
     * you can manage every {@link CloudPlayer} and {@link CloudOfflinePlayer}
     * @see PlayerManager
     */
    @Nonnull
    public abstract PlayerManager getPlayerManager();

    /**
     * The current {@link ServiceManager} instance where
     * you can manage every {@link ServiceInfo}
     * @see ServiceManager
     */
    @Nonnull
    public abstract ServiceManager getServiceManager();

    /**
     * The current {@link ModuleManager} instance where
     * you can manage every {@link Module}
     * @see ModuleManager
     */
    @Nonnull
    public abstract ModuleManager getModuleManager();

    /**
     * The current {@link ServiceTaskManager} instance where
     * you can manage every {@link ServiceTask} and {@link TaskGroup}
     * @see ServiceTaskManager
     */
    @Nonnull
    public abstract ServiceTaskManager getServiceTaskManager();

    /**
     * The current {@link AdvancedNetworkExecutor} instance
     * @see AdvancedNetworkExecutor
     */
    public abstract AdvancedNetworkExecutor getExecutor();

    public abstract T thisSidesClusterParticipant();

    /**
     * Returns the {@link DriverStatus} to gain information about current Cloud Build
     * By retrieving the Annotation at the top of the class
     */
    @Nonnull
    public DriverStatus status() {
        return CloudDriver.class.getAnnotation(DriverStatus.class);
    }

}

