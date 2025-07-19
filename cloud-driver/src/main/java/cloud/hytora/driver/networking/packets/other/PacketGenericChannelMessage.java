package cloud.hytora.driver.networking.packets.other;

import cloud.hytora.document.Document;
import cloud.hytora.driver.common.message.base.def.DefaultChannelMessage;
import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.networking.protocol.SimpleNetworkComponent;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import cloud.hytora.driver.networking.protocol.types.BufferState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PacketGenericChannelMessage extends AbstractPacket {

    private String channel;
    private long startTime;
    private String className;
    private String jsonData;
    private Collection<NetworkComponent> receivers;

    private boolean channelMessageObject;
    private DefaultChannelMessage channelMessage;

    public PacketGenericChannelMessage(String channel, Object object, NetworkComponent... receivers) {
        this.channel = channel;
        this.startTime = System.currentTimeMillis();
        this.receivers = Arrays.asList(receivers);

        this.channelMessageObject = object instanceof DefaultChannelMessage;
        if (this.channelMessageObject) {
            this.channelMessage = (DefaultChannelMessage) object;
            this.channelMessage.setChannel(channel);
        } else {
            this.className = object.getClass().getName();
            this.jsonData = Document.gson(object).asRawJsonString();
        }
    }

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {

        switch (state) {

            case READ:
                this.channelMessageObject = buf.readBoolean();

                channel = buf.readString();
                startTime = buf.readLong();

                if (channelMessageObject) {
                    channelMessage = buf.readObject(DefaultChannelMessage.class);
                } else {
                    className = buf.readString();
                    jsonData = buf.readString();
                }
                receivers = buf.readWrapperObjectCollection(SimpleNetworkComponent.class);
                break;

            case WRITE:
                buf.writeBoolean(channelMessageObject);

                buf.writeString(channel);
                buf.writeLong(startTime);
                if (channelMessageObject) {
                    buf.writeObject(channelMessage);
                } else {
                    buf.writeString(className);
                    buf.writeString(jsonData);
                }
                buf.writeObjectCollection(receivers);
                break;
        }
    }
}
