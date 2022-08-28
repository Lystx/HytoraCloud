package cloud.hytora.driver.networking.protocol.codec.buf.defaults;

import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BufferedCustom<T extends IBufferObject> implements AbstractBuffered<BufferedCustom, T> {

    private T wrapped;

    @Override
    public void setWrapped(BufferedCustom wrapped) {
        this.wrapped = (T) wrapped.getWrapped();
    }

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
