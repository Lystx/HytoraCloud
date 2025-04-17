package cloud.hytora.modules.sign.cloud;

import cloud.hytora.common.logging.Logger;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.module.ModuleController;
import cloud.hytora.driver.module.controller.AbstractModule;
import cloud.hytora.driver.module.controller.base.ModuleConfiguration;
import cloud.hytora.driver.module.controller.base.ModuleCopyType;
import cloud.hytora.driver.module.controller.base.ModuleEnvironment;
import cloud.hytora.driver.module.controller.base.ModuleState;
import cloud.hytora.driver.module.controller.task.ModuleTask;
import cloud.hytora.driver.module.controller.task.ScheduledModuleTask;
import cloud.hytora.modules.sign.api.CloudSignAPI;
import cloud.hytora.modules.sign.api.config.SignConfiguration;
import cloud.hytora.modules.sign.cloud.command.ModuleCloudSignCommand;
import cloud.hytora.modules.sign.cloud.handler.ModuleMessageHandler;
import cloud.hytora.modules.sign.cloud.listener.ModuleServiceReadyListener;

@ModuleConfiguration(
        name = "module-signs",
        main = ModuleBootstrap.class,
        author = "Lystx",
        description = "Manages the sign selectors",
        version = "SNAPSHOT-1.0",
        website = "https://github.com/Lystx/HytoraCloud/tree/master/cloud-modules/module-signs",
        copyType = ModuleCopyType.SERVER_FALLBACK,
        environment = ModuleEnvironment.ALL
)
public class ModuleBootstrap extends AbstractModule {

    public ModuleBootstrap(ModuleController controller) {
        super(controller);
    }

    @ModuleTask(id = 1, state = ModuleState.LOADED)
    public void load() {
        new ModuleCloudSignAPI(this);
        SignConfiguration configuration;
        Logger.constantInstance().debug("Loading signs config...");
        if (controller.getConfig().isEmpty()) {
            controller.getConfig().set(configuration = new SignConfiguration());
            controller.getConfig().save();
        } else {
            configuration = controller.getConfig().toInstance(SignConfiguration.class);
        }

        //setting value in api
        CloudSignAPI.getInstance().setSignConfiguration(configuration);
        Logger.constantInstance().info("Loaded config {}", CloudSignAPI.getInstance().getSignConfiguration());
    }

    @ModuleTask(id = 2, state = ModuleState.ENABLED)
    @ScheduledModuleTask(sync = false, delay = 100)
    public void enable() {
        CloudDriver.getInstance()
                .getChannelMessenger()
                .registerChannel(
                        CloudSignAPI.CHANNEL_NAME,
                        new ModuleMessageHandler()
                );

        CloudDriver.getInstance().getEventManager().registerListener(new ModuleServiceReadyListener());
        CloudDriver.getInstance().getCommandManager().registerCommand(new ModuleCloudSignCommand());


        CloudSignAPI.getInstance().publishConfiguration();
        CloudSignAPI.getInstance().getSignManager().update();
        CloudDriver.getInstance().getLogger().info("§aSuccessfully enabled Module §fCloud-Signs§a!");
    }


    @ModuleTask(id = 3, state = ModuleState.DISABLED)
    public void disable() {
        CloudDriver.getInstance().getCommandManager().unregisterCommand(ModuleCloudSignCommand.class);
        CloudDriver.getInstance().getChannelMessenger().unregisterChannel(CloudSignAPI.CHANNEL_NAME);
    }

}
