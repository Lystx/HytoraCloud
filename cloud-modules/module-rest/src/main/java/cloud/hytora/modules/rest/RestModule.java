package cloud.hytora.modules.rest;

import cloud.hytora.driver.module.ModuleController;
import cloud.hytora.driver.module.controller.AbstractModule;
import cloud.hytora.driver.module.controller.base.ModuleState;
import cloud.hytora.driver.module.controller.task.ModuleTask;
import lombok.Getter;

public class RestModule extends AbstractModule {


    @Getter
    private final ModuleController controller;

    public RestModule(ModuleController controller) {
        this.controller = controller;
    }

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
