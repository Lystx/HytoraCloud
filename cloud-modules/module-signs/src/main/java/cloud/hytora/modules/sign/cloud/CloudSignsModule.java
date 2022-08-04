package cloud.hytora.modules.sign.cloud;

import cloud.hytora.common.logging.Logger;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.message.ChannelMessage;
import cloud.hytora.driver.message.ChannelMessageBuilder;
import cloud.hytora.driver.module.controller.DriverModule;
import cloud.hytora.driver.module.controller.base.ModuleConfiguration;
import cloud.hytora.driver.module.controller.base.ModuleCopyType;
import cloud.hytora.driver.module.controller.base.ModuleEnvironment;
import cloud.hytora.driver.module.controller.base.ModuleState;
import cloud.hytora.driver.module.controller.task.ModuleTask;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.modules.sign.api.CloudSign;
import cloud.hytora.modules.sign.api.config.SignConfiguration;
import cloud.hytora.modules.sign.api.protocol.SignProtocolType;
import cloud.hytora.modules.sign.cloud.manager.SignManager;
import lombok.Getter;

import java.util.function.Consumer;

@ModuleConfiguration(
        name = "module-signs",
        main = CloudSignsModule.class,
        author = "Lystx",
        description = "Manages the sign selectors",
        version = "SNAPSHOT-1.0",
        website = "https://github.com/Lystx/HytoraCloud/tree/master/cloud-modules/module-signs",
        copyType = ModuleCopyType.SERVER_FALLBACK,
        environment = ModuleEnvironment.ALL
)
public class CloudSignsModule extends DriverModule {

    public static final String CHANNEL_NAME = "cloud::modules::signs::channel";

    /**
     * The static module instance
     */
    @Getter
    private static CloudSignsModule instance;

    /**
     * The loaded config
     */
    private SignConfiguration configuration;

    private SignManager signManager;


    @ModuleTask(id = 1, state = ModuleState.LOADED)
    public void load() {
        instance = this;
        signManager = new SignManager();
        signManager.loadSigns().onTaskSucess(cloudSigns -> {
           Logger.constantInstance().info("Signs-Module loaded {} Cloudsigns!", cloudSigns.size());
        });
    }

    @ModuleTask(id = 2, state = ModuleState.LOADED)
    public void loadConfig() {
        Logger.constantInstance().debug("Loading signs config...");
        if (controller.getConfig().isEmpty()) {
            controller.getConfig().set(configuration = new SignConfiguration());
            controller.getConfig().save();
        } else {
            configuration = controller.getConfig().toInstance(SignConfiguration.class);
        }
        Logger.constantInstance().debug("Loaded config {}", configuration);
    }

    @ModuleTask(id = 3, state = ModuleState.ENABLED)
    public void enable() {
        CloudDriver.getInstance().getChannelMessenger().registerChannel(CHANNEL_NAME, new Consumer<ChannelMessage>() {
            @Override
            public void accept(ChannelMessage channelMessage) {
                PacketBuffer buffer = channelMessage.buffer();
                switch (buffer.readEnum(SignProtocolType.class)) {
                    case ADD_SIGN:
                        CloudSign cloudSign = buffer.readObject(CloudSign.class);
                        signManager.addCloudSign(cloudSign);
                        break;
                    case REMOVE_SIGN:
                        CloudSign sign = buffer.readObject(CloudSign.class);
                        CloudSign safeSign = signManager.getSign(sign.getUuid());
                        signManager.removeCloudSign(safeSign);
                        break;
                }
            }
        });

        this.updateConfig();
        this.updateSigns();
    }


    @ModuleTask(id = 4, state = ModuleState.DISABLED)
    public void disable() {
        CloudDriver.getInstance().getChannelMessenger().unregisterChannel(CHANNEL_NAME);
    }


    public void updateSigns() {
        performAction(SignProtocolType.SYNC_CACHE, buf -> {
            buf.writeObjectCollection(this.signManager.getCachedCloudSigns());
        });
    }

    public void updateConfig() {
        performAction(SignProtocolType.SYNC_CONFIG, buf -> {
            buf.writeDocument(DocumentFactory.newJsonDocument(this.configuration));
        });
    }


    public void performAction(SignProtocolType protocolType, Consumer<PacketBuffer> buf) {
        ChannelMessage message = ChannelMessage.builder().channel(CHANNEL_NAME).buffer(buf).build();
        message.send();
    }
}
