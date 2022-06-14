package cloud.hytora.modules.rest;

import cloud.hytora.driver.module.controller.DriverModule;
import cloud.hytora.driver.module.controller.base.ModuleConfiguration;
import cloud.hytora.driver.module.controller.base.ModuleCopyType;
import cloud.hytora.driver.module.controller.base.ModuleEnvironment;
import cloud.hytora.driver.module.controller.base.ModuleState;
import cloud.hytora.driver.module.controller.task.ModuleTask;

@ModuleConfiguration(
        name = "module-rest",
        main = RestModule.class,
        author = "Lystx",
        description = "Manages the rest api",
        version = "SNAPSHOT-1.0",
        website = "https://github.com/Lystx/HytoraCloud/tree/master/cloud-modules/module-rest",
        copyType = ModuleCopyType.NONE,
        environment = ModuleEnvironment.NODE
)
public class RestModule extends DriverModule {

    @ModuleTask(id = 1, state = ModuleState.LOADED)
    public void load() {
    }

    @ModuleTask(id = 2, state = ModuleState.ENABLED)
    public void enable() {

    }

    @ModuleTask(id = 3, state = ModuleState.DISABLED)
    public void disable() {

    }

}
