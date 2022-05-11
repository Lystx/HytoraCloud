package cloud.hytora.driver;

import cloud.hytora.common.collection.NamedThreadFactory;
import cloud.hytora.common.logging.Logger;
import cloud.hytora.driver.command.CommandManager;
import cloud.hytora.driver.command.Console;
import cloud.hytora.driver.event.EventManager;
import cloud.hytora.driver.event.defaults.DefaultEventManager;
import cloud.hytora.driver.message.ChannelMessenger;
import cloud.hytora.driver.networking.AdvancedNetworkExecutor;
import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.node.NodeManager;
import cloud.hytora.driver.player.PlayerManager;
import cloud.hytora.driver.services.ServiceManager;
import cloud.hytora.driver.services.configuration.ConfigurationManager;
import cloud.hytora.driver.storage.DriverStorage;
import cloud.hytora.driver.command.sender.CommandSender;

import cloud.hytora.driver.networking.PacketProvider;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.JdkLoggerFactory;
import lombok.Getter;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Getter
@DriverStatus(version = "SNAPSHOT-0.1", experimental = true, developers = {"Lystx"})
public abstract class CloudDriver {

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
     * The default event manager
     */
    protected final EventManager eventManager;

    /**
     * The java executor service
     */
    protected final ScheduledExecutorService scheduledExecutor;

    public CloudDriver(Logger logger, DriverEnvironment environment) {
        instance = this;

        this.environment = environment;
        this.logger = logger;
        this.eventManager = new DefaultEventManager();
        this.scheduledExecutor = Executors.newScheduledThreadPool(4, new NamedThreadFactory("Scheduler"));

        // use jdk logger to prevent issues with older slf4j versions
        // like them bundled in spigot 1.8

        try {
            JdkLoggerFactory.class.getDeclaredField("INSTANCE");
            InternalLoggerFactory.setDefaultFactory(JdkLoggerFactory.INSTANCE);
        } catch (NoSuchFieldException e) {
            this.logger.error("Couldn't override Netty Logger to prevent slf4j-spam!");
        }

        // check if the leak detection level is set before overriding it
        // may be useful for debugging of the network
        if (System.getProperty("io.netty.leakDetection.level") == null) {
            ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.DISABLED);
        }
        PacketProvider.registerPackets();
    }

    public abstract void shutdown();

    public abstract void logToExecutor(NetworkComponent component, String message, Object... args);

    @Nullable
    @CheckReturnValue
    public abstract Console getConsole();

    @Nonnull
    public abstract DriverStorage getStorage();

    @Nonnull
    public abstract CommandSender getCommandSender();

    @Nonnull
    public abstract NodeManager getNodeManager();

    @Nonnull
    public abstract ChannelMessenger getChannelMessenger();

    @Nonnull
    public abstract CommandManager getCommandManager();

    @Nonnull
    public abstract PlayerManager getPlayerManager();

    @Nonnull
    public abstract ServiceManager getServiceManager();

    @Nonnull
    public abstract ConfigurationManager getConfigurationManager();

    @Nonnull
    public abstract AdvancedNetworkExecutor getExecutor();

    @Nonnull
    @CheckReturnValue
    public DriverStatus status() {
        return CloudDriver.class.getAnnotation(DriverStatus.class);
    }
}

