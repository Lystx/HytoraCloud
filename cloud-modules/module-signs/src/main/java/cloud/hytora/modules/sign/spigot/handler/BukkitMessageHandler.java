package cloud.hytora.modules.sign.spigot.handler;

import cloud.hytora.driver.message.ChannelMessage;
import cloud.hytora.driver.message.ChannelMessageListener;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.modules.sign.api.def.UniversalCloudSign;
import cloud.hytora.modules.sign.api.CloudSignAPI;
import cloud.hytora.modules.sign.api.ICloudSignManager;
import cloud.hytora.modules.sign.api.config.SignConfiguration;
import cloud.hytora.modules.sign.api.protocol.SignProtocolType;

public class BukkitMessageHandler implements ChannelMessageListener {

    @Override
    public void handleIncoming(ChannelMessage message) {

        ICloudSignManager signManager = CloudSignAPI.getInstance().getSignManager();
        PacketBuffer buffer = message.buffer();

        switch (buffer.readEnum(SignProtocolType.class)) {
            case SYNC_CACHE:
                signManager.setAllCachedCloudSigns(buffer.readWrapperObjectCollection(UniversalCloudSign.class));
                break;
            case SYNC_CONFIG:
                CloudSignAPI.getInstance().setSignConfiguration(buffer.readDocument().toInstance(SignConfiguration.class));
                break;
        }
    }
}
