package cloud.hytora.driver.networking.protocol.packets;

import cloud.hytora.driver.common.LoggingDriver;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;


/**
 * The {@link PacketHandler} is the core of the networking.
 * It is used to handle incoming {@link IPacket}s and respond to them or perform other actions.
 *
 * @author Lystx
 * @version DEV-1.0
 * @since DEV-0.9
 * @param <T> the generic type of the packet
 *
 * @see PacketChannel
 * @see IPacket
 */
public interface PacketHandler<T extends IPacket> extends LoggingDriver {

    /**
     * The method that is being called when the {@link IPacket}
     * has been decoded and now wants to be handled
     *
     * @param channel the handling channel
     * @param packet the packet that is being handled
     */
    void handle(PacketChannel channel, T packet);
}
