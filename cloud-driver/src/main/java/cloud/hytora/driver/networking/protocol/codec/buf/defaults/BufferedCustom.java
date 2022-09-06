package cloud.hytora.driver.networking.protocol.codec.buf.defaults;

import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

@Data
@AllArgsConstructor
@Setter
public class BufferedCustom<T extends IBufferObject> implements IBuffered<BufferedCustom, T> {

    private T wrapped;

    @Override
    public Class<BufferedCustom> getWrapperClass() {
        return BufferedCustom.class;
    }

    @Override
    public T read(PacketBuffer buffer) {
        Class objectClass = buffer.readClass();
        return (T) buffer.readObject(objectClass);
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeClass(wrapped.getClass());
        buffer.writeObject(wrapped);
    }
}
