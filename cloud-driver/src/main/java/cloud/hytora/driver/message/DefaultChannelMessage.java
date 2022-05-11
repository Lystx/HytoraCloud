package cloud.hytora.driver.message;

import cloud.hytora.document.Document;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.networking.protocol.SimpleNetworkComponent;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.UUID;

@Getter @Setter @AllArgsConstructor
public class DefaultChannelMessage implements ChannelMessage {

    /**
     * The key of this message
     */
    private String key;

    /**
     * The channel it should be send to
     */
    private String channel;

    /**
     * The document
     */
    private Document document;

    /**
     * The receiver
     */
    private NetworkComponent[] receivers;

    /**
     * The sender
     */
    private NetworkComponent sender;

    /**
     * The uuid of this message
     */
    private UUID id;

    @Override
    public String toString() {
        return document.asFormattedJsonString();
    }

    @Override
    public void send() {
        CloudDriver.getInstance().getChannelMessenger().sendChannelMessage(this);
    }

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {

        switch (state) {

            case READ:
                key = buf.readString();
                channel = buf.readString();
                document = buf.readDocument();
                receivers = buf.readObjectArray(SimpleNetworkComponent.class);
                sender = buf.readOptionalObject(SimpleNetworkComponent.class);
                id = buf.readUniqueId();
                break;

            case WRITE:
                buf.writeString(key);
                buf.writeString(channel);
                buf.writeDocument(document);
                buf.writeObjectArray(receivers);
                buf.writeOptionalObject(sender);
                buf.writeUniqueId(id);
                break;
        }
    }

}
