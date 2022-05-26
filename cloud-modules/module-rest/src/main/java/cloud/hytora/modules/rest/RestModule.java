package cloud.hytora.modules.rest;

import cloud.hytora.driver.module.controller.DriverModule;
import cloud.hytora.driver.module.controller.base.ModuleState;
import cloud.hytora.driver.module.controller.task.ModuleTask;

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
