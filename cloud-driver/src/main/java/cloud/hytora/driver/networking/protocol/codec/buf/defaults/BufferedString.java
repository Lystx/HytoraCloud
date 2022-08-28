package cloud.hytora.driver.networking.protocol.codec.buf.defaults;

import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@Data
@AllArgsConstructor
public class BufferedString implements AbstractBuffered<BufferedString, String> {

    private String wrapped;

    @Override
    public void setWrapped(BufferedString wrapped) {
        this.wrapped = wrapped.getWrapped();
    }

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
