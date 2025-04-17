package cloud.hytora.driver.message;

import cloud.hytora.document.Document;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.networking.protocol.packets.BufferState;

public interface DocumentPacket {

    String getChannel();

    void handleData(BufferState state, Document document);

    default void publish() {
        CloudDriver.getInstance().getChannelMessenger().sendDocumentPacket(this);
    }
    default void publish(NetworkComponent... receivers) {
        CloudDriver.getInstance().getChannelMessenger().sendDocumentPacket(this, receivers);
    }
}
