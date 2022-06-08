package cloud.hytora.modules.proxy;

import cloud.hytora.common.logging.Logger;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.module.controller.DriverModule;
import cloud.hytora.driver.module.controller.base.ModuleConfiguration;
import cloud.hytora.driver.module.controller.base.ModuleCopyType;
import cloud.hytora.driver.module.controller.base.ModuleEnvironment;
import cloud.hytora.driver.module.controller.base.ModuleState;
import cloud.hytora.driver.module.controller.task.ModuleTask;
import cloud.hytora.modules.proxy.config.*;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.File;

@ModuleConfiguration(
        name = "module-proxy",
        main = ProxyModule.class,
        author = "Lystx",
        description = "Manages the Proxy servers",
        version = "SNAPSHOT-1.0",
        website = "https://github.com/Lystx/HytoraCloud/tree/master/cloud-modules/module-proxy",
        copyType = ModuleCopyType.PROXY,
        environment = ModuleEnvironment.ALL
)
public class ProxyModule extends DriverModule {


    private final Logger logger = Logger.newInstance();

    /**
     * The static module instance
     */
    @Getter
    private static ProxyModule instance;

    /**
     * The config to communicate between module <->
     */
    private ProxyConfig proxyConfig;

    @ModuleTask(id = 1, state = ModuleState.LOADED)
    public void load() {
        instance = this;

        loadConfig();
    }

    @ModuleTask(id = 2, state = ModuleState.ENABLED)
    public void enable() {

    }

    @ModuleTask(id = 3, state = ModuleState.DISABLED)
    public void disable() {

    }


    private void loadConfig() {
        logger.info("Loading proxy config...");
        if (controller.getConfig().isEmpty()) {
            controller.getConfig().set(proxyConfig = ProxyConfig.defaultConfig());
            controller.getConfig().save();
        } else {
            proxyConfig = controller.getConfig().toInstance(ProxyConfig.class);
        }
        CloudDriver.getInstance().getStorage().set("proxyConfig", proxyConfig).update();
        logger.info("Loaded config {}", proxyConfig);
    }
}
