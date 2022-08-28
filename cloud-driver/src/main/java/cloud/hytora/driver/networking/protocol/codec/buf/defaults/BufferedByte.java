package cloud.hytora.driver.networking.protocol.codec.buf.defaults;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BufferedByte implements AbstractBuffered<BufferedByte, Byte> {

    private Byte wrapped;

    @Override
    public void setWrapped(BufferedByte wrapped) {
        this.wrapped = wrapped.getWrapped();
    }

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
