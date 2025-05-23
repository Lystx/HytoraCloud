package cloud.hytora.driver.message;

import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.networking.protocol.SimpleNetworkComponent;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import lombok.AccessLevel;
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
    private String key = "";

    /**
     * The channel it should be send to
     */
    private String channel;

    /**
     * The document
     */
    private Document document = DocumentFactory.emptyDocument();

    private PacketBuffer buffer =  PacketBuffer.unsafe();

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
    public PacketBuffer buffer() {
        return buffer;
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
                buffer = buf.readBuffer();
                break;

            case WRITE:
                buf.writeString(key);
                buf.writeString(channel);
                buf.writeDocument(document);
                buf.writeObjectArray(receivers);
                buf.writeOptionalObject(sender);
                buf.writeUniqueId(id);
                buf.writeBuffer(buffer);
                break;
        }
    }

    @Override
    public void receiver(NetworkComponent component) {
        this.receivers = new NetworkComponent[]{component};
    }
}
