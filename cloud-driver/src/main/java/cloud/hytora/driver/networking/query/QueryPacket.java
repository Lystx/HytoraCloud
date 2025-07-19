package cloud.hytora.driver.networking.query;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import cloud.hytora.driver.networking.protocol.types.BufferState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class QueryPacket extends AbstractPacket {

    private UUID internalId;
    private String channel;
    private String key;

    private PacketBuffer packetBuffer;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        switch (state) {
            case WRITE:
                buf.writeUniqueId(this.internalId);
                buf.writeString(channel);
                buf.writeString(key);

                buf.writeBuffer(packetBuffer);

                break;

            case READ:
                this.internalId = buf.readUniqueId();
                this.channel = buf.readString();
                this.key = buf.readString();

                this.packetBuffer = buf.readBuffer();
                break;
        }
    }
}
