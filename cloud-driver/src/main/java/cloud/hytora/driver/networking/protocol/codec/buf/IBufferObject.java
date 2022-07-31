package cloud.hytora.driver.networking.protocol.codec.buf;

import cloud.hytora.driver.networking.protocol.packets.BufferState;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Serializable objects can be sent in packets using {@link PacketBuffer#writeObject(IBufferObject)} and {@link PacketBuffer#readObject(Class)}.
 * An implementation of SerializableObject must have an empty constructor (can be {@code private})
 */
public interface IBufferObject {

	void applyBuffer(BufferState state, @Nonnull PacketBuffer buf) throws IOException;

}
