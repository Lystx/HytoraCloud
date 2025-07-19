package cloud.hytora.driver.networking;

import cloud.hytora.driver.networking.protocol.packets.IPacket;

/**
 * This interface declares that the inheriting object is able
 * to send {@link IPacket}s
 * To understand which {@link IPacket} goes where from where, look at {@link #sendPacket(IPacket)}
 *
 * @author Lystx
 * @since DEV-0.1
 * @version DEV-0.3
 */
public interface PacketSender {

    /**
     * Sends a {@link IPacket} to the next component
     * Execution-Models:
     *
     *  proxy/minecraft => CLOUD
     *  CLOUD => ALL servers
     *
     * @param packet the packet to send
     */
    void sendPacket(IPacket packet);

}
