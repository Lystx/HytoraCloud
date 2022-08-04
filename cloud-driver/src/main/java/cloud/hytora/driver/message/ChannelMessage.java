package cloud.hytora.driver.message;

import cloud.hytora.document.Document;
import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;

import java.util.UUID;

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
