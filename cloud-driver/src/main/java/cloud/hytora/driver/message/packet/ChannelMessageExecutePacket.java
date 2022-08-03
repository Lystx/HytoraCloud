package cloud.hytora.driver.message.packet;

import cloud.hytora.driver.message.ChannelMessage;
import cloud.hytora.driver.message.DefaultChannelMessage;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ChannelMessageExecutePacket extends AbstractPacket {

    private ChannelMessage channelMessage;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {

        switch (state) {

            case READ:
                channelMessage = buf.readObject(DefaultChannelMessage.class);
                break;

            case WRITE:
                buf.writeObject(channelMessage);
                break;
        }
    }
}
