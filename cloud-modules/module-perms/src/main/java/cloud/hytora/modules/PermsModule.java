package cloud.hytora.modules;

import cloud.hytora.driver.module.controller.DriverModule;
import cloud.hytora.driver.module.controller.base.ModuleConfiguration;
import cloud.hytora.driver.module.controller.base.ModuleCopyType;
import cloud.hytora.driver.module.controller.base.ModuleEnvironment;
import cloud.hytora.driver.module.controller.base.ModuleState;
import cloud.hytora.driver.module.controller.task.ModuleTask;

@ModuleConfiguration(
        name = "module-perms",
        main = PermsModule.class,
        author = "Lystx",
        description = "Implementation for integrated Permissions-System",
        version = "SNAPSHOT-1.0",
        website = "https://github.com/Lystx/HytoraCloud/tree/master/cloud-modules/module-perms",
        copyType = ModuleCopyType.ALL,
        environment = ModuleEnvironment.ALL
)
public class PermsModule extends DriverModule {



    @ModuleTask(id = 1, state = ModuleState.LOADED)
    public void load() {

    }

    @ModuleTask(id = 2, state = ModuleState.ENABLED)
    public void enable() {

    }
}
