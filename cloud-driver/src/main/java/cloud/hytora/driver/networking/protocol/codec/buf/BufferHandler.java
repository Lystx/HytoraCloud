package cloud.hytora.driver.networking.protocol.codec.buf;

import cloud.hytora.common.function.BiSupplier;

import java.util.function.BiConsumer;

public interface BufferHandler extends BiSupplier<PacketBuffer, Object>, BiConsumer<Object, PacketBuffer> {
}
