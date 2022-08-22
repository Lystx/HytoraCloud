package cloud.hytora.modules.notify;

import cloud.hytora.context.annotations.Constructor;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.commands.ICommandManager;
import cloud.hytora.driver.event.IEventManager;
import cloud.hytora.driver.module.ModuleController;
import cloud.hytora.driver.module.controller.AbstractModule;
import cloud.hytora.driver.module.controller.base.ModuleConfiguration;
import cloud.hytora.driver.module.controller.base.ModuleCopyType;
import cloud.hytora.driver.module.controller.base.ModuleEnvironment;
import cloud.hytora.driver.module.controller.base.ModuleState;
import cloud.hytora.driver.module.controller.task.ModuleTask;
import cloud.hytora.modules.notify.command.NotifyCommand;
import cloud.hytora.modules.notify.config.NotifyConfiguration;
import cloud.hytora.modules.notify.listener.ModuleListener;
import lombok.Getter;

public class NotifyModule {

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

    @Getter
    private final ModuleController controller;

    @Constructor
    public NotifyModule(ModuleController moduleController) {
        instance = this;

        this.controller = moduleController;
    }

    @ModuleTask(id = 1, state = ModuleState.LOADED)
    public void loadConfig() {

        if (controller.getConfig().isEmpty()) {
            controller.getConfig().set(configuration = new NotifyConfiguration());
            controller.getConfig().save();
        } else {
            configuration = controller.getConfig().toInstance(NotifyConfiguration.class);
        }
    }

    @ModuleTask(id = 2, state = ModuleState.ENABLED)
    public void enable() {
        //registering command and listener
        CloudDriver.getInstance().getProviderRegistry().getUnchecked(IEventManager.class).registerListener(new ModuleListener());
        CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICommandManager.class).registerCommands(new NotifyCommand());
    }

}
