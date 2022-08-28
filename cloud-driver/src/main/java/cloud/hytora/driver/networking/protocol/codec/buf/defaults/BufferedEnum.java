package cloud.hytora.driver.networking.protocol.codec.buf.defaults;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BufferedEnum implements AbstractBuffered<BufferedEnum, Enum> {

    private Enum wrapped;

    @Override
    public void setWrapped(BufferedEnum wrapped) {
        this.wrapped = wrapped.getWrapped();
    }

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
