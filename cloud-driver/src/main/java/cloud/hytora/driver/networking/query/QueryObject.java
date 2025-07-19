package cloud.hytora.driver.networking.query;

import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;

import java.util.UUID;
import java.util.function.Consumer;

public interface QueryObject<T extends QueryObject> {

    UUID getInternalId();

    T setInternalId(UUID id);

    String getChannel();

    T setChannel(String channel);

    String getKey();

    T setKey(String key);

    PacketBuffer getBuffer();

    T setBuffer(Consumer<PacketBuffer> buffer);



}
