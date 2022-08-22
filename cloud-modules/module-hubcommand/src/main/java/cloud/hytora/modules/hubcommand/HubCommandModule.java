package cloud.hytora.modules.hubcommand;

import cloud.hytora.context.annotations.Constructor;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.commands.ICommandManager;
import cloud.hytora.driver.module.ModuleController;
import cloud.hytora.driver.module.controller.base.ModuleState;
import cloud.hytora.driver.module.controller.task.ModuleTask;
import cloud.hytora.modules.hubcommand.command.HubCommand;

public class HubCommandModule {

    private final ModuleController controller;

    @Constructor
    public HubCommandModule(ModuleController controller) {
        this.controller = controller;
    }

    @ModuleTask(id = 1, state = ModuleState.ENABLED)
    public void enable() {
        CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICommandManager.class).registerCommands(new HubCommand());
    }

    @ModuleTask(id = 3, state = ModuleState.DISABLED)
    public void disable() {
        CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICommandManager.class).unregister(HubCommand.class);
    }
}
