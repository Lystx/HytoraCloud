package cloud.hytora.driver.networking.packets.response;


import cloud.hytora.document.Document;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.wrapped.PacketAction;

import java.util.UUID;

/**
 * A {@link BufferedResponse} is the response to a query using a {@link PacketAction}.
 * You send the response in form of a {@link PacketAction} to set all values and later
 * receive a {@link BufferedResponse} to access and get all set values
 *
 * @author Lystx
 * @version STABLE-1.0
 * @since STABLE-1.0
 * @see PacketAction
 */
public interface BufferedResponse {

    /**
     * The state of this response
     */
    NetworkResponseState state();

    /**
     * The name of the sender of this response
     */
    String sender();

    /**
     * The buffer that was returned
     */
    PacketBuffer buffer();

    /**
     * The error if occurred
     */
    Throwable error();

    /**
     * The uuid of this response
     */
    UUID uniqueId();

    /**
     * The provided data for this response
     */
    Document data();
}
