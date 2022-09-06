package cloud.hytora.driver.networking.protocol.codec.buf.defaults;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

@Data
@AllArgsConstructor
@Setter
public class BufferedString implements IBuffered<BufferedString, String> {

    private String wrapped;

    @Override
    public Class<BufferedString> getWrapperClass() {
        return BufferedString.class;
    }

    @Override
    public String read(PacketBuffer buffer) {
        return buffer.readString();
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeString(wrapped);
    }
}
