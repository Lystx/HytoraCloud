package cloud.hytora.driver.networking.protocol.codec.buf.defaults;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

import java.util.UUID;

@Data
@AllArgsConstructor
@Setter
public class BufferedUUID implements IBuffered<BufferedUUID, UUID> {

    private UUID wrapped;

    @Override
    public Class<BufferedUUID> getWrapperClass() {
        return BufferedUUID.class;
    }

    @Override
    public UUID read(PacketBuffer buffer) {
        return buffer.readUniqueId();
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeUniqueId(wrapped);
    }
}
