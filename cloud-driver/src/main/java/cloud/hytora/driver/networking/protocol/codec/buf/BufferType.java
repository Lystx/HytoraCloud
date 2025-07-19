package cloud.hytora.driver.networking.protocol.codec.buf;

import cloud.hytora.common.function.BiSupplier;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

@AllArgsConstructor
@Getter
public enum BufferType {

    STRING(String.class, PacketBuffer::readString, (input, buf) -> buf.writeString((String) input)),
    INTEGER(Integer.class, PacketBuffer::readInt, (input, buf) -> buf.writeInt((Integer) input)),
    DOUBLE(Double.class, PacketBuffer::readDouble, (input, buf) -> buf.writeDouble((Double) input)),
    FLOAT(Float.class, PacketBuffer::readFloat, (input, buf) -> buf.writeFloat((Float) input)),
    LONG(Long.class, PacketBuffer::readLong, (input, buf) -> buf.writeLong((Long) input)),
    BOOLEAN(Boolean.class, PacketBuffer::readBoolean, (input, buf) -> buf.writeBoolean((Boolean) input))
    ;


    private final Class<?> typeClass;
    private final BiSupplier<PacketBuffer, Object> readFunction;
    private final BiConsumer<Object, PacketBuffer> writeFunction;



    private static final Map<Class<?>, BufferHandler> handlers = new HashMap<>();


    public static void registerHandler(Class<?> typeClass, BufferHandler handler) {
        handlers.put(typeClass, handler);
    }

    public static BufferHandler getHandler(Class<?> typeClass) {
        Optional<BufferHandler> bufferHandler = Arrays.stream(values())
                .filter(type -> type.getTypeClass().equals(typeClass))
                .findFirst()
                .map(type -> new BufferHandler() {
                    @Override
                    public Object supply(PacketBuffer buffer) {
                        return type.getReadFunction().supply(buffer);
                    }

                    @Override
                    public void accept(Object o, PacketBuffer buffer) {
                        type.getWriteFunction().accept(o, buffer);
                    }
                });
        return bufferHandler.orElseGet(() -> handlers.get(typeClass));
    }

}
