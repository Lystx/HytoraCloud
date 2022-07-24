package cloud.hytora.modules.notify;

import cloud.hytora.common.scheduler.Scheduler;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.module.controller.DriverModule;
import cloud.hytora.driver.module.controller.base.ModuleConfiguration;
import cloud.hytora.driver.module.controller.base.ModuleCopyType;
import cloud.hytora.driver.module.controller.base.ModuleEnvironment;
import cloud.hytora.driver.module.controller.base.ModuleState;
import cloud.hytora.driver.module.controller.task.ModuleTask;
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
public class NotifyModule extends DriverModule {

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

    public NotifyModule() {
        instance = this;
    }

    @ModuleTask(id = 1, state = ModuleState.LOADED)
    public void loadConfig() {
        CloudDriver.getInstance().getLogger().info("Loading notify config...");
        if (controller.getConfig().isEmpty()) {
            controller.getConfig().set(configuration = new NotifyConfiguration());
            controller.getConfig().save();
        } else {
            configuration = controller.getConfig().toInstance(NotifyConfiguration.class);
        }
        CloudDriver.getInstance().getLogger().info("Loaded config {}", configuration);
    }

    @ModuleTask(id = 2, state = ModuleState.ENABLED)
    public void enable() {

        CloudDriver.getInstance().getLogger().info("Registering Event & Command for Notify-Module", configuration);
        //registering command and listener
        this.registerEvent(new ModuleListener());
        this.registerCommand(new NotifyCommand());
    }

}
