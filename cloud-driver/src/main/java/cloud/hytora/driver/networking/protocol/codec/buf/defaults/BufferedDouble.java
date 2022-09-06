package cloud.hytora.driver.networking.protocol.codec.buf.defaults;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

@Data
@AllArgsConstructor
@Setter
public class BufferedDouble implements IBuffered<BufferedDouble, Double> {

    private Double wrapped;

    @Override
    public Class<BufferedDouble> getWrapperClass() {
        return BufferedDouble.class;
    }

    @Override
    public Double read(PacketBuffer buffer) {
        return buffer.readDouble();
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeDouble(wrapped);
    }
}
