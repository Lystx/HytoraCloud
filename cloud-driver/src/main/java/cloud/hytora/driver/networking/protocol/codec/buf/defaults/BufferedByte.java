package cloud.hytora.driver.networking.protocol.codec.buf.defaults;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

@Data
@AllArgsConstructor
@Setter
public class BufferedByte implements IBuffered<BufferedByte, Byte> {

    private Byte wrapped;

    @Override
    public Class<BufferedByte> getWrapperClass() {
        return BufferedByte.class;
    }

    @Override
    public Byte read(PacketBuffer buffer) {
        return buffer.readByte();
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeByte(wrapped);
    }
}
