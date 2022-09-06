package cloud.hytora.driver.networking.protocol.codec.buf.defaults;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

@Data
@AllArgsConstructor
@Setter
public class BufferedEnum implements IBuffered<BufferedEnum, Enum> {

    private Enum wrapped;

    @Override
    public Class<BufferedEnum> getWrapperClass() {
        return BufferedEnum.class;
    }

    @Override
    public Enum read(PacketBuffer buffer) {
        Class eClass = buffer.readClass();
        return buffer.readEnum(eClass);
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeClass(wrapped.getClass());
        buffer.writeEnum(wrapped);
    }
}
