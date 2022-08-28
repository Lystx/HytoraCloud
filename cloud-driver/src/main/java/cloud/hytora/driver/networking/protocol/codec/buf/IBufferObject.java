package cloud.hytora.driver.networking.protocol.codec.buf;

import cloud.hytora.driver.networking.protocol.packets.BufferState;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Objects that implement {@link IBufferObject} can be sent in packets using <br>
 * {@link PacketBuffer#writeObject(IBufferObject)} and {@link PacketBuffer#readObject(Class)}.
 * An implementation of {@link IBufferObject} must have an empty constructor (may not be {@code private}!)
 *
 * @author Lystx
 * @since SNAPSHOT-1.0
 */
public interface IBufferObject {

	/**
	 * Applies data of this object to the provided {@link PacketBuffer}
	 *
	 * @param state the state to indicate if reading or writing
	 * @param buf the buffer to append to
	 * @throws IOException if something goes wrong while reading/writing
	 */
	void applyBuffer(BufferState state, @Nonnull PacketBuffer buf) throws IOException;

}
