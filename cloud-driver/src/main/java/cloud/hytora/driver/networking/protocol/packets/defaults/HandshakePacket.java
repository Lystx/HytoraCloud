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
public class HandshakePacket extends Packet {

    /**
     * The authentication key
     */
    private String authKey;

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

    /**
     * The authentication node
     */
    private String nodeName;

    public HandshakePacket(String authKey, String clientName, ConnectionType type, Document extraData) {
        this.authKey = authKey;
        this.clientName = clientName;
        this.type = type;
        this.extraData = extraData;
    }

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        switch (state) {

            case READ:
                authKey = buf.readString();
                type = buf.readEnum(ConnectionType.class);
                clientName = buf.readString();
                extraData = buf.readDocument();
                nodeName = buf.readOptionalString();
                break;

            case WRITE:
                buf.writeString(authKey);
                buf.writeEnum(this.type);
                buf.writeString(this.clientName);
                buf.writeDocument(extraData);
                buf.writeOptionalString(nodeName);
                break;
        }
    }

}
