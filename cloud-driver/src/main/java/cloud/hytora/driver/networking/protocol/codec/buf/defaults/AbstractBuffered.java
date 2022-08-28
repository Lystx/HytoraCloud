package cloud.hytora.driver.networking.protocol.codec.buf.defaults;

import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface AbstractBuffered<T extends IBufferObject, R> extends IBufferObject {

    R getWrapped();

    Class<T> getWrapperClass();

    void setWrapped(T wrapped);

    R read(PacketBuffer buffer);

    void write(PacketBuffer buffer);

    @Override
    default void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        switch (state) {
            case WRITE:
                write(buf);
                break;
            case READ:
                setWrapped((T) read(buf));
                break;
        }
    }
}
