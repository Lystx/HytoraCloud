package cloud.hytora.modules.sign.cloud;

import cloud.hytora.common.logging.Logger;
import cloud.hytora.document.Document;
import cloud.hytora.driver.common.message.base.ChannelMessage;
import cloud.hytora.driver.module.ModuleInfo;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.modules.sign.api.CloudSignAPI;
import cloud.hytora.modules.sign.api.ICloudSignManager;
import cloud.hytora.modules.sign.api.protocol.SignProtocolType;
import cloud.hytora.modules.sign.cloud.manager.ModuleCloudSignManager;
import lombok.Getter;

import java.util.function.Consumer;

@Getter
public class ModuleCloudSignAPI extends CloudSignAPI {

    private final ICloudSignManager signManager;

    public ModuleCloudSignAPI(ModuleInfo module) {
        super();

        this.signManager = new ModuleCloudSignManager(module.getController().getDataFolder());
        this.signManager.loadCloudSignsAsync().onTaskSucess(cloudSigns -> {
            Logger.constantInstance().info("The %1LobbySignSelectorModule §7loaded %2{} CloudSigns§8!", cloudSigns.size());
        });
    }

    @Override
    public void publishConfiguration() {
        this.performProtocolAction(
                SignProtocolType.SYNC_CONFIG,
                buf -> buf.writeDocument(
                        Document.gson(
                                this.getSignConfiguration()
                        )
                )
        );
    }

    @Override
    public void performProtocolAction(SignProtocolType type, Consumer<PacketBuffer> buffer) {

        ChannelMessage message = ChannelMessage
                .builder()
                .channel(CloudSignAPI.CHANNEL_NAME)
                .buffer(buf -> {
                    buf.writeEnum(type);
                    buf.append(buffer);
                }).build();
        message.send();
    }
}

