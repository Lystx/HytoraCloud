package cloud.hytora.driver.networking.protocol.codec.buf.defaults;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

@Data
@AllArgsConstructor
@Setter
public class BufferedBoolean implements IBuffered<BufferedBoolean, Boolean> {

    private Boolean wrapped;

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
