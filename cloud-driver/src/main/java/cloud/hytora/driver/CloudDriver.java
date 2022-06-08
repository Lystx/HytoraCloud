package cloud.hytora.driver;

import cloud.hytora.common.DriverUtility;
import cloud.hytora.common.collection.NamedThreadFactory;
import cloud.hytora.common.logging.Logger;
import cloud.hytora.driver.command.CommandManager;
import cloud.hytora.driver.command.Console;
import cloud.hytora.driver.event.EventManager;
import cloud.hytora.driver.event.defaults.DefaultEventManager;
import cloud.hytora.driver.http.api.HttpServer;
import cloud.hytora.driver.message.ChannelMessenger;
import cloud.hytora.driver.module.ModuleManager;
import cloud.hytora.driver.networking.AdvancedNetworkExecutor;
import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.node.NodeManager;
import cloud.hytora.driver.player.PlayerManager;
import cloud.hytora.common.scheduler.Scheduler;
import cloud.hytora.driver.services.ServiceManager;
import cloud.hytora.driver.services.task.ServiceTaskManager;
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

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Getter
@DriverStatus(version = "SNAPSHOT-0.1", experimental = true, developers = {"Lystx"})
public abstract class CloudDriver extends DriverUtility {

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

    public static final int SERVER_PUBLISH_INTERVAL = 90_000; // publish all 1.5 minutes
    public static final int SERVER_CYCLE_TIMEOUT = 2; // service times out after 3 minutes
    public static final String APPLICATION_NAME = "Application"; //the name for the Dashboard

    public CloudDriver(Logger logger, DriverEnvironment environment) {
        instance = this;

        this.environment = environment;
        this.logger = logger;
        this.eventManager = new DefaultEventManager();
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





        // check if the leak detection level is set before overriding it
        // may be useful for debugging of the network
        if (System.getProperty("io.netty.leakDetection.level") == null) {
            ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.DISABLED);
        }
        PacketProvider.registerPackets();

        this.running = true;
    }

    public abstract void shutdown();

    public abstract void logToExecutor(NetworkComponent component, String message, Object... args);

    public void logToExecutorAndSelf(NetworkComponent component, String message, Object... args) {
        this.logToExecutor(component, message, args);
        this.logger.info(message, args);
    }


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

    public void executeIf(Runnable runnable, Supplier<Boolean> request) {
        this.executeIf(runnable, request, TimeUnit.DAYS.toMillis(1));
    }

    @Nullable
    @CheckReturnValue
    public abstract Console getConsole();

    @Nullable
    @CheckReturnValue
    public abstract HttpServer getHttpServer();

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
    public abstract ModuleManager getModuleManager();

    @Nonnull
    public abstract ServiceTaskManager getServiceTaskManager();

    public abstract AdvancedNetworkExecutor getExecutor();

    @Nonnull
    @CheckReturnValue
    public DriverStatus status() {
        return CloudDriver.class.getAnnotation(DriverStatus.class);
    }

}

