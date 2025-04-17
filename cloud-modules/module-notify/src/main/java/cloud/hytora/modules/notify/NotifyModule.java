package cloud.hytora.modules.notify;

import cloud.hytora.common.scheduler.Scheduler;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.message.ChannelMessage;
import cloud.hytora.driver.message.ChannelMessageListener;
import cloud.hytora.driver.module.ModuleController;
import cloud.hytora.driver.module.controller.AbstractModule;
import cloud.hytora.driver.module.controller.base.ModuleConfiguration;
import cloud.hytora.driver.module.controller.base.ModuleCopyType;
import cloud.hytora.driver.module.controller.base.ModuleEnvironment;
import cloud.hytora.driver.module.controller.base.ModuleState;
import cloud.hytora.driver.module.controller.task.ModuleTask;
import cloud.hytora.driver.module.controller.task.ScheduledModuleTask;
import cloud.hytora.modules.notify.command.NotifyCommand;
import cloud.hytora.modules.notify.config.NotifyConfiguration;
import cloud.hytora.modules.notify.listener.ModuleListener;
import lombok.Getter;

@ModuleConfiguration(
        name = "module-notify",
        main = NotifyModule.class,
        author = "Lystx",
        description = "Shows notifications when services start/stop",
        version = "SNAPSHOT-1.0",
        website = "https://github.com/Lystx/HytoraCloud/tree/master/cloud-modules/module-notify",
        copyType = ModuleCopyType.NONE,
        environment = ModuleEnvironment.NODE
)
public class NotifyModule extends AbstractModule {

    /**
     * The static module instance
     */
    @Getter
    private static NotifyModule instance;

    /**
     * The configuration of this module
     */
    @Getter
    private NotifyConfiguration configuration;

    public NotifyModule(ModuleController controller) {
        super(controller);
        instance = this;
    }

    @ModuleTask(id = 1, state = ModuleState.LOADED)
    public void loadConfig() {
        configuration = new NotifyConfiguration();
        CloudDriver.getInstance().getLogger().debug("============");
        CloudDriver.getInstance().getLogger().debug("Loading notify config...");
        if (controller.getConfig().isEmpty()) {
            CloudDriver.getInstance().getLogger().debug("Empty notify config...");
            controller.getConfig().set(configuration = new NotifyConfiguration());
            controller.getConfig().save();
        } else {
            CloudDriver.getInstance().getLogger().debug("Existing notify config...");
            configuration = controller.getConfig().toInstance(NotifyConfiguration.class);
        }
        CloudDriver.getInstance().getLogger().debug("Loaded config {}", configuration);
        CloudDriver.getInstance().getLogger().debug("============");
    }

    @ModuleTask(id = 2, state = ModuleState.ENABLED)
    public void enable() {

        CloudDriver.getInstance().getLogger().info("Registering Event & Command for Notify-Module", configuration);
        //registering command and listener
        this.registerEvent(new ModuleListener());

        this.registerCommand(new NotifyCommand());
        CloudDriver.getInstance().getLogger().info("Registered Event & Command for Notify-Module", configuration);

    }

}



