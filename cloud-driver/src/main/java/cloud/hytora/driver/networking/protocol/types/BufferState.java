package cloud.hytora.driver.networking.protocol.types;

import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;

/**
 * The {@link BufferState} defines the state of an {@link IBufferObject} that is being
 * handled right now. So it knows if it should read or write its data.
 *
 * @author Lystx
 * @since DEV-1.0
 * @version DEV-1.0
 *
 * @see PacketBuffer
 * @see IBufferObject
 */
public enum BufferState {

    /**
     * This object is reading its
     * data from a {@link PacketBuffer}
     */
    READ,

    /**
     * This object is writing its
     * data to a {@link PacketBuffer}
     */
    WRITE
}
