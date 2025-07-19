package cloud.hytora.driver.networking.protocol.packets;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.networking.packets.response.BufferedResponse;
import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.info.PacketTransferInfo;
import cloud.hytora.driver.networking.protocol.wrapped.PacketAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The {@link IPacket} is the core of the cloud-networking to exchange data across the network
 *
 * It contains a {@link PacketBuffer} to store data, a {@link PacketTransferInfo} to determine
 * the identification of this packet.
 *
 * {@link IPacket}s can respond to other packets or can create queries to request data from other instances.
 * using {@link #sendQuery()} or {@link #sendResponse()}
 *
 *
 * @author Lystx
 * @since DEV-0.1
 * @version SNAPSHOT-1.5
 */
public interface IPacket extends IBufferObject {

    /**
     * Returns the current {@link PacketTransferInfo}
     * @see PacketTransferInfo
     */
    PacketTransferInfo transferInfo();

    /**
     * Returns the actual buffer of this packet
     * If not set it will return null
     *
     * @return buffer instance or null
     */
    @Nullable
    PacketBuffer buffer();

    /**
     * This method retrieves the current {@link PacketBuffer}
     * The difference from {@link #buffer()} is that if no buffer
     * is set yet it will return {@link PacketBuffer#unPooled()}
     *
     * @return the buffer
     */
    @NotNull
    PacketBuffer getBufferSafe();

    /**
     * Publishes this packet from the current
     * {@link cloud.hytora.driver.networking.NetworkExecutor} instance
     * that is defined in the {@link cloud.hytora.driver.CloudDriver}.
     *
     * Execution models:
     *
     * => From Server => To Node (STOP)
     * => From Node => To every Server (STOP)
     */
    void publish();

    /**
     * Calls {@link #publish()} but sends packet async
     * @see #publish()
     */
    Task<Void> publishAsync();

    /**
     * Publishes this packet to every connected instance
     * Execution models:
     *
     * => From Server => To Node => To every server (STOP)
     * => From Node => To every Server (STOP)
     */
    void publishToAll();

    /**
     * Publishes this packet to specified instances
     * Execution models:
     *
     * => From Server => To Node => To specified instances (STOP)
     * => From Node => To specified instance (STOP)
     *
     * @param receivers the specified receiver names
     */
    void publishTo(String... receivers);

    /**
     * Prepares a response for this packet
     *
     * @see PacketAction
     */
    PacketAction<Void> sendResponse();

    /**
     * Prepares a query for this packet
     *
     * @see PacketAction
     */
    PacketAction<BufferedResponse> sendQuery();

}
