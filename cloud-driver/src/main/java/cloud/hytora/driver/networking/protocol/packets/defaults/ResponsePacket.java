package cloud.hytora.driver.networking.protocol.packets.defaults;

import cloud.hytora.document.Document;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferedResponse;
import cloud.hytora.driver.networking.protocol.packets.NetworkResponseState;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResponsePacket extends AbstractPacket implements BufferedResponse {

    /**
     * The name of the connection that responded
     */
    private String responderName;

    private Throwable error;

    /**
     * The state of this response
     */
    private NetworkResponseState state;

    /**
     * The responded data for this response
     */
    private Document data;

    /**
     * Extra data
     */
    private PacketBuffer buffer;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        switch (state) {

            case READ:
                responderName = buf.readString();
                error = buf.readOptionalThrowable();
                this.state = buf.readEnum(NetworkResponseState.class);
                data = buf.readOptionalDocument();

                this.buffer = buf.readBuffer();
                break;

            case WRITE:
                buf.writeString(responderName);
                buf.writeOptionalThrowable(error);
                buf.writeEnum(state);
                buf.writeOptionalDocument(data);
                buf.writeBuffer(buffer);
                break;
        }
    }

    @Override
    public PacketBuffer buffer() {
        return buffer;
    }

    @Override
    public Throwable error() {
        return error;
    }

    @Override
    public NetworkResponseState state() {
        return state;
    }

    @Override
    public String sender() {
        return responderName;
    }

    @Override
    public Document data() {
        return data;
    }

    @Override
    public UUID uniqueId() {
        return transferInfo.getInternalQueryId();
    }
}
