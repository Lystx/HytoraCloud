package cloud.hytora.driver.networking.protocol.codec.buf.defaults;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

@Data
@AllArgsConstructor
@Setter
public class BufferedLong implements IBuffered<BufferedLong, Long> {

    private Long wrapped;

    @Override
    public Class<BufferedLong> getWrapperClass() {
        return BufferedLong.class;
    }

    @Override
    public Long read(PacketBuffer buffer) {
        return buffer.readLong();
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeLong(wrapped);
    }
}
