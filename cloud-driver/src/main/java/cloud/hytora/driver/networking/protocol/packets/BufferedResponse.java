package cloud.hytora.driver.networking.protocol.packets;


import cloud.hytora.document.Document;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;

import java.util.UUID;

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
     * The uuid of this response
     */
    UUID uniqueId();

    /**
     * The provided data for this response
     */
    Document data();
}
