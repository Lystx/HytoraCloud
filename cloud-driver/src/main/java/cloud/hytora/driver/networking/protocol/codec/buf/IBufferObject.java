package cloud.hytora.driver.networking.protocol.codec.buf;

import cloud.hytora.driver.networking.protocol.types.BufferState;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * Serializable objects can be sent in packets using {@link PacketBuffer#writeObject(IBufferObject)} and {@link PacketBuffer#readObject(Class)}.
 * An implementation of SerializableObject must have an empty constructor (can be {@code private})
 */
public interface IBufferObject {

	/**
	 * Called when applying the {@link PacketBuffer} in different {@link BufferState}
	 * Here you read/write all the data from/to the object
	 *
	 * @param state the state to check
	 * @param buf the buffer to work with
	 * @throws IOException if something internally went wrong
	 *
	 * @see PacketBuffer
	 */
	void applyBuffer(BufferState state, @Nonnull PacketBuffer buf) throws IOException;


}
