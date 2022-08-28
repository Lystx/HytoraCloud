package cloud.hytora.driver.networking.protocol.codec.buf.defaults;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BufferedLong implements AbstractBuffered<BufferedLong, Long> {

    private Long wrapped;

    @Override
    public void setWrapped(BufferedLong wrapped) {
        this.wrapped = wrapped.getWrapped();
    }

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
