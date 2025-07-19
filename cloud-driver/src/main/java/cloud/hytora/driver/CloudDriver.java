package cloud.hytora.driver;

import cloud.hytora.common.Holder;
import cloud.hytora.common.collection.NamedThreadFactory;
import cloud.hytora.common.collection.pair.Tuple;
import cloud.hytora.common.function.BiSupplier;
import cloud.hytora.common.logging.Logger;
import cloud.hytora.driver.command.CommandManager;
import cloud.hytora.driver.config.ConfigManager;
import cloud.hytora.driver.entity.player.CloudOfflinePlayer;
import cloud.hytora.driver.entity.player.CloudPlayer;
import cloud.hytora.driver.entity.player.PlayerFullJoinExecutor;
import cloud.hytora.driver.entity.player.PlayerManager;
import cloud.hytora.driver.event.EventManager;
import cloud.hytora.driver.event.defaults.DefaultEventManager;
import cloud.hytora.driver.common.http.api.HttpRequest;
import cloud.hytora.driver.common.http.api.HttpServer;
import cloud.hytora.driver.common.message.base.ChannelMessage;
import cloud.hytora.driver.common.message.base.ChannelMessenger;
import cloud.hytora.driver.language.LanguageManager;
import cloud.hytora.driver.module.ModuleInfo;
import cloud.hytora.driver.module.ModuleManager;
import cloud.hytora.driver.networking.HandlingNetworkExecutor;
import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.networking.packets.DefaultPacketProcessoring;
import cloud.hytora.driver.networking.protocol.PacketTypeProcessor;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import cloud.hytora.driver.entity.node.INode;
import cloud.hytora.driver.entity.node.NodeManager;
import cloud.hytora.common.scheduler.Scheduler;
import cloud.hytora.driver.entity.player.impl.DefaultFullJoinExecutor;
import cloud.hytora.driver.common.provider.defaults.DefaultProviderRegistry;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.entity.services.ServiceManager;
import cloud.hytora.driver.entity.services.task.ServiceTask;
import cloud.hytora.driver.entity.services.task.ServiceTaskManager;
import cloud.hytora.driver.entity.services.task.bundle.TaskGroup;
import cloud.hytora.driver.entity.services.template.TemplateManager;
import cloud.hytora.driver.entity.services.template.def.DefaultTemplateManager;
import cloud.hytora.driver.command.sender.CommandSender;

import cloud.hytora.driver.networking.packets.PacketRegistry;
import cloud.hytora.driver.common.tps.TickWorker;
import cloud.hytora.driver.common.tps.def.DefaultTickWorker;

import io.netty.util.ResourceLeakDetector;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.JdkLoggerFactory;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


/**
 * The <b>CloudDriver</b> is the core of the API of HytoraCloud.
 * It allows the System internally and developers to make use of every Manager across the Network
 * For example you can get information about a specific {@link CloudPlayer}, a specific {@link CloudService},
 * a specific {@link ServiceTask}, a specific {@link TaskGroup}. <br>
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
     * The dir formatter for every service
     */
    protected final BiSupplier<Tuple<File, CloudService>, File> serverDirectoryFormatter;

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
     * If memory usage is too high, the node will automatically
     * enable this option to disable multi threadding
     * Then everything will be running in one thread because the JVM
     * can't create any more threads.
     * It would still be better to shut down the cloudSystem at this point,
     * when the given notification is visible.
     */
    @Setter
    protected boolean singleThread;

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

        this.setProvider(PacketTypeProcessor.class, new DefaultPacketProcessoring());

        //registering all packets
        PacketRegistry.registerPackets();

        this.running = true; //set running state

        //setting first provider
        this.setProvider(PlayerFullJoinExecutor.class, new DefaultFullJoinExecutor());

        this.serverDirectoryFormatter = pair -> {
            File parent = pair.getFirst();
            CloudService service = pair.getSecond();

            String groupName = service.getTask().getTaskGroup().getName();
            boolean sameName = false;
            for (ServiceTask child : service.getTask().getTaskGroup().getChildren()) {
                if (child.getName().equalsIgnoreCase(groupName)) {
                    sameName = true;
                }
            }

            if (sameName) {
                //if same Group and Task name (e.g. Group = "Lobby", Task = "Lobby" <- no need for double folders
                //this only makes sense when e.g. (Group = "BedWars", Task = "BedWars-8x1" <- multiple folders make sense for clean dir managing
                return new File(parent, service.getTask().getTaskGroup().getName() + "/" + service.getName() + "@" + service.getUniqueId());
            }
            return new File(parent, service.getTask().getTaskGroup().getName() + "/" + service.getTask().getName() + "/" + service.getName() + "@" + service.getUniqueId());
        };
    }

    /**
     * Shuts down the current Driver Instance
     * (Node, Remote or whatever extends this class)
     */
    public abstract void shutdown();


    public abstract LanguageManager getLanguageManager();

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
     * you can manage every {@link CloudPlayer} and {@link CloudOfflinePlayer}
     * @see PlayerManager
     */
    @Nonnull
    public abstract PlayerManager getPlayerManager();

    /**
     * The current {@link ServiceManager} instance where
     * you can manage every {@link CloudService}
     * @see ServiceManager
     */
    @Nonnull
    public abstract ServiceManager getServiceManager();

    /**
     * The current {@link ModuleManager} instance where
     * you can manage every {@link ModuleInfo}
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
     * The current {@link HandlingNetworkExecutor} instance
     * @see HandlingNetworkExecutor
     */
    public abstract HandlingNetworkExecutor getExecutor();

    /**
     * The current {@link ConfigManager} instance
     * @see ConfigManager
     */
    public abstract ConfigManager getConfigManager();

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
         * The environment is a Node
         */
        HEADNODE,

        /**
         * The Environment is a Remote
         */
        SERVICE;


        public Environment getOpposite() {
            if (this == NODE || this == HEADNODE) {
                return SERVICE;
            } else {
                return NODE;
            }
        }

    }

    public static class Constants {



        public static final File NODE_FOLDER = new File("cloud/");
        public static final File CONFIG_FILE = new File(NODE_FOLDER, "config.json");
        public static final File LOG_FOLDER = new File(NODE_FOLDER, "logs/");
        public static final File LOG_FOLDER_EXTRA = new File(NODE_FOLDER, "logs/cachedSaves");
        public static File MODULE_FOLDER;

        public static final File STORAGE_FOLDER = new File(NODE_FOLDER, "storage/");
        public static final File DATABASE_FOLDER = new File(STORAGE_FOLDER, "database/");
        public static final File STORAGE_VERSIONS_FOLDER = new File(STORAGE_FOLDER, "versions/");
        public static final File STORAGE_TEMP_FOLDER = new File(STORAGE_FOLDER, "tmp-" + UUID.randomUUID().toString().substring(0, 5) + "/");
        public static final File TEMPLATES_DIR = new File(STORAGE_FOLDER, "templates/");

        public static final File SERVICE_DIR = new File(NODE_FOLDER, "services/");
        public static final File SERVICE_DIR_STATIC = new File(SERVICE_DIR, "permanent/");
        public static final File SERVICE_DIR_DYNAMIC = new File(SERVICE_DIR, "temporary/");

        /**
         * the query channel for player requests
         */
        public static final String QUERY_CHANNEL_PLAYER = "hytora_cloud_player";
        public static final String QUERY_KEY_PLAYER_CHECK_LOGIN = "check_login_proxy";
        public static final String QUERY_CHANNEL_SERVER = "hytora_cloud_service";
        public static final String QUERY_CHANNEL_OFFLINE_PLAYER = "hytora_cloud_player_offline";

        /**
         * The module-bridge file for communication between cloud and minecraft
         */
        public static final String BRIDGE_FILE_NAME = "cloud_bridge.jar";

        /**
         * the remote file
         */
        public static final String REMOTE_FILE_NAME = "cloud_remote.jar";


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
    }

    //explained by itself
    public void setSingleThread(boolean singleThread) {
        this.singleThread = singleThread;
        Holder.SINGLE_THREAD = singleThread;
    }
}

