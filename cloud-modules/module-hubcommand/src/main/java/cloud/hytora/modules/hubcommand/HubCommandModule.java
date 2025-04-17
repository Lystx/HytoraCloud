package cloud.hytora.modules.hubcommand;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.module.ModuleController;
import cloud.hytora.driver.module.controller.AbstractModule;
import cloud.hytora.driver.module.controller.base.ModuleConfiguration;
import cloud.hytora.driver.module.controller.base.ModuleCopyType;
import cloud.hytora.driver.module.controller.base.ModuleEnvironment;
import cloud.hytora.driver.module.controller.base.ModuleState;
import cloud.hytora.driver.module.controller.task.ModuleTask;
import cloud.hytora.modules.hubcommand.command.HubCommand;

@ModuleConfiguration(
        name = "module-hubcommand",
        main = HubCommandModule.class,
        author = "Lystx",
        description = "Sends you to a fallback",
        version = "SNAPSHOT-1.0",
        website = "https://github.com/Lystx/HytoraCloud/tree/master/cloud-modules/module-hubcommand",
        copyType = ModuleCopyType.NONE,
        environment = ModuleEnvironment.NODE
)
public class HubCommandModule extends AbstractModule {


    public HubCommandModule(ModuleController controller) {
        super(controller);
    }

    @ModuleTask(id = 1, state = ModuleState.ENABLED)
    public void enable() {
        this.registerCommand(new HubCommand());
    }

    @ModuleTask(id = 3, state = ModuleState.DISABLED)
    public void disable() {
        CloudDriver.getInstance().getCommandManager().unregisterCommand(HubCommand.class);
    }
}
