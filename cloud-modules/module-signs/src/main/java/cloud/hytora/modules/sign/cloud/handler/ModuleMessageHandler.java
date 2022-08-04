package cloud.hytora.modules.sign.cloud.handler;

import cloud.hytora.driver.message.ChannelMessage;
import cloud.hytora.driver.message.ChannelMessageListener;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.modules.sign.api.def.UniversalCloudSign;
import cloud.hytora.modules.sign.api.CloudSignAPI;
import cloud.hytora.modules.sign.api.ICloudSign;
import cloud.hytora.modules.sign.api.ICloudSignManager;
import cloud.hytora.modules.sign.api.protocol.SignProtocolType;

public class ModuleMessageHandler implements ChannelMessageListener {

    @Override
    public void handleIncoming(ChannelMessage message) {
        ICloudSignManager signManager = CloudSignAPI.getInstance().getSignManager();
        PacketBuffer buffer = message.buffer();

        switch (buffer.readEnum(SignProtocolType.class)) {
            case ADD_SIGN:
                ICloudSign cloudSign = buffer.readObject(UniversalCloudSign.class);
                signManager.addCloudSign(cloudSign);
                break;
            case REMOVE_SIGN:
                ICloudSign sign = buffer.readObject(UniversalCloudSign.class);
                ICloudSign safeSign = signManager.getCloudSignOrNull(sign.getUniqueId());
                signManager.removeCloudSign(safeSign);
                break;
        }
    }
}
