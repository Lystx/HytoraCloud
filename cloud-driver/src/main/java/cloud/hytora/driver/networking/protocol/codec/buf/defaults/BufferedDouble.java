package cloud.hytora.driver.networking.protocol.codec.buf.defaults;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BufferedDouble implements AbstractBuffered<BufferedDouble, Double> {

    private Double wrapped;

    @Override
    public void setWrapped(BufferedDouble wrapped) {
        this.wrapped = wrapped.getWrapped();
    }

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
