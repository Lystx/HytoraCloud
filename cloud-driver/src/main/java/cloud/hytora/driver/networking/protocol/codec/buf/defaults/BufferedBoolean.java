package cloud.hytora.driver.networking.protocol.codec.buf.defaults;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BufferedBoolean implements AbstractBuffered<BufferedBoolean, Boolean> {

    private Boolean wrapped;

    @Override
    public void setWrapped(BufferedBoolean wrapped) {
        this.wrapped = wrapped.getWrapped();
    }

    @Override
    public Class<BufferedBoolean> getWrapperClass() {
        return BufferedBoolean.class;
    }

    @Override
    public Boolean read(PacketBuffer buffer) {
        return buffer.readBoolean();
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeBoolean(wrapped);
    }
}
