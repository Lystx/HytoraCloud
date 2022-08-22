package cloud.hytora.modules.sign.cloud;

import cloud.hytora.common.logging.Logger;
import cloud.hytora.context.annotations.Constructor;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.commands.ICommandManager;
import cloud.hytora.driver.event.IEventManager;
import cloud.hytora.driver.message.IChannelMessenger;
import cloud.hytora.driver.module.ModuleController;
import cloud.hytora.driver.module.controller.AbstractModule;
import cloud.hytora.driver.module.controller.base.ModuleState;
import cloud.hytora.driver.module.controller.task.ModuleTask;
import cloud.hytora.modules.sign.api.CloudSignAPI;
import cloud.hytora.modules.sign.api.config.SignConfiguration;
import cloud.hytora.modules.sign.cloud.command.ModuleCloudSignCommand;
import cloud.hytora.modules.sign.cloud.handler.ModuleMessageHandler;
import cloud.hytora.modules.sign.cloud.listener.ModuleServiceReadyListener;
import lombok.Getter;

public class ModuleBootstrap {

    @Getter
    private final ModuleController controller;

    @Constructor
    public ModuleBootstrap(ModuleController controller) {
        this.controller = controller;
    }

    @ModuleTask(id = 1, state = ModuleState.LOADED)
    public void load() {
        new ModuleCloudSignAPI(this);
    }

    @ModuleTask(id = 2, state = ModuleState.LOADED)
    public void loadConfig() {
        SignConfiguration configuration;
        if (controller.getConfig().isEmpty()) {
            controller.getConfig().set(configuration = new SignConfiguration());
            controller.getConfig().save();
        } else {
            configuration = controller.getConfig().toInstance(SignConfiguration.class);
        }

        //setting value in api
        CloudSignAPI.getInstance().setSignConfiguration(configuration);
    }

    @ModuleTask(id = 3, state = ModuleState.ENABLED)
    public void enable() {
        CloudDriver.getInstance().getProviderRegistry().getUnchecked(IChannelMessenger.class).registerChannel(CloudSignAPI.CHANNEL_NAME, new ModuleMessageHandler());
        CloudDriver.getInstance().getProviderRegistry().getUnchecked(IEventManager.class).registerListener(new ModuleServiceReadyListener());
        CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICommandManager.class).registerCommands(new ModuleCloudSignCommand());


        CloudSignAPI.getInstance().publishConfiguration();
        CloudSignAPI.getInstance().getSignManager().update();
    }


    @ModuleTask(id = 4, state = ModuleState.DISABLED)
    public void disable() {
        CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICommandManager.class).unregister(ModuleCloudSignCommand.class);
        CloudDriver.getInstance().getProviderRegistry().getUnchecked(IChannelMessenger.class).unregisterChannel(CloudSignAPI.CHANNEL_NAME);
    }

}
