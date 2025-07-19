package cloud.hytora.driver.common.message.base;

import cloud.hytora.document.Document;
import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;

import java.util.UUID;

/**
 * A {@link ChannelMessage} is used to send custom data across the network
 * without having to worry about creating/registering/sending new custom {@link AbstractPacket}s
 *
 * A ChannelMessage has following essential content:
 *
 *  => Channel (to determine whether it has the right "categorie" that it got sent to)
 *  => Key (to determine within the channel if it has the right "identifier")
 *  => Document (to append custom data in form of a JsonObject)
 *  => PacketBuffer (to append custom data that does not need to be parsed/unparsed like json)
 *
 * @since STABLE-1.0
 * @version STABLE-1.5
 * @author Lystx
 *
 * @see IBufferObject
 * @see Document
 * @see PacketBuffer
 */
public interface ChannelMessage extends IBufferObject {

    /**
     * Creates a new builder
     *
     * @return builder
     */
    static ChannelMessageBuilder builder() {
        return new ChannelMessageBuilder();
    }

    /**
     * Gets a key of this message
     * @return message key
     */
    String getKey();

    /**
     * The channel of this message
     *
     * @return channel
     */
    String getChannel();

    /**
     * The request id of this message
     *
     * @return id
     */
    UUID getId();

    /**
     * Sets the id of this message
     *
     * @param id the id
     */
    void setId(UUID id);

    /**
     * Sets the receiver of this message as a {@link NetworkComponent}
     *
     * @param component the receiver
     */
    void receiver(NetworkComponent component);

    /**
     * The data of this message
     *
     * @return data
     */
    Document getDocument();

    /**
     * The network buffer
     */
    PacketBuffer buffer();

    /**
     * The receiver of this message
     *
     * @return receiver
     */
    NetworkComponent[] getReceivers();

    /**
     * The sender of this message
     *
     * @return sender
     */
    NetworkComponent getSender();

    /**
     * Sends this message
     */
    void send();
}
