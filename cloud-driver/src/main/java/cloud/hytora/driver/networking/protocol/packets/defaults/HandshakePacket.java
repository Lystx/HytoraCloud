package cloud.hytora.driver.networking.protocol.packets.defaults;

import cloud.hytora.document.Document;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.networking.protocol.packets.ConnectionType;
import cloud.hytora.driver.networking.protocol.packets.Packet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HandshakePacket extends Packet {

    /**
     * The provided name
     */
    private String clientName;

    /**
     * The type of the participant
     */
    private ConnectionType type;

    /**
     * The extra data
     */
    private Document extraData;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        switch (state) {

            case READ:
                type = buf.readEnum(ConnectionType.class);
                clientName = buf.readString();
                extraData = buf.readDocument();
                break;

            case WRITE:
                buf.writeEnum(this.type);
                buf.writeString(this.clientName);
                buf.writeDocument(extraData);
                break;
        }
    }

}
