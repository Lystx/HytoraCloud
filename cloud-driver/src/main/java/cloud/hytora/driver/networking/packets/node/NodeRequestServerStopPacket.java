package cloud.hytora.driver.networking.packets.node;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class NodeRequestServerStopPacket extends AbstractPacket {

    private String serverName;

    private boolean demandsResponse;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {

        switch (state) {

            case READ:
                serverName = buf.readString();
                demandsResponse = buf.readBoolean();
                break;

            case WRITE:
                buf.writeString(serverName);
                buf.writeBoolean(demandsResponse);
                break;
        }
    }
}
