package cloud.hytora.driver.networking.protocol.packets.info;

import cloud.hytora.document.Document;
import cloud.hytora.driver.networking.NetworkComponent;

import java.util.UUID;

/**
 * A {@link PacketTransferInfo} is used internally to determine where
 * an incoming query belongs to and so on.
 * In general it defines the internal {@link UUID} that an {@link cloud.hytora.driver.networking.protocol.packets.IPacket} gets.
 * It also provides information like the header of a Packet or the sender of it.
 *
 *
 * @author Lystx
 * @since DEV-1.0
 * @version SNAPSHOT-1.5
 */
public interface PacketTransferInfo {

    /**
     * The internal query {@link UUID} of this packet
     */
    UUID getInternalQueryId();

    /**
     * Sets the internal query id of this packet
     *
     * @param queryId the id to set
     */
    void setInternalQueryId(UUID queryId);

    /**
     * The sender of this packet
     *
     * @see NetworkComponent
     */
    NetworkComponent getSender();

    /**
     * The header of this packet
     *
     * @see Document
     */
    Document getHeader();

}
