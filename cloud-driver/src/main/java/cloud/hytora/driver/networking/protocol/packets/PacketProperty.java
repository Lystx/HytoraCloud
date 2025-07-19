package cloud.hytora.driver.networking.protocol.packets;

/**
 * {@link PacketProperty}s are used to define properties when
 * constructing a new {@link AbstractPacket}
 *
 * @since STABLE-2.0
 * @version STABLE-2.0
 * @author Lystx
 */
public enum PacketProperty {

    /**
     * This property defines, that
     * no new {@link cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer}
     * should be created
     */
    NO_BUFFER,
}
