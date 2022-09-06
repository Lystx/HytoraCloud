package cloud.hytora.driver.networking.protocol.codec.buf.defaults;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

@Data
@AllArgsConstructor
@Setter
public class BufferedInt implements IBuffered<BufferedInt, Integer> {

    private Integer wrapped;


    @Override
    public Class<BufferedInt> getWrapperClass() {
        return BufferedInt.class;
    }

    @Override
    public Integer read(PacketBuffer buffer) {
        return buffer.readInt();
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeInt(wrapped);
    }
}
