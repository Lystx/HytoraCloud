package cloud.hytora.driver.networking;

import cloud.hytora.common.function.BiSupplier;
import cloud.hytora.driver.networking.protocol.packets.IPacket;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A {@link HandlingNetworkExecutor} is built on the plattform of a {@link NetworkExecutor}
 * The difference is, that this instance can handle incoming {@link IPacket}s.
 * This means, that it can also manage {@link PacketHandler}s and its {@link PacketChannel}
 *
 * @see NetworkExecutor
 * @since DEV-1.0
 * @version STABLE-1.0
 */
public interface HandlingNetworkExecutor extends NetworkExecutor {

    /**
     * The core method that is called when an {@link IPacket} was decoded
     * and is now ready to be handled.
     *
     * @param channel the channel that it got decoded from
     * @param packet the packet instance
     * @param <T> the generic type of packet
     */
    <T extends IPacket> void handlePacket(@Nullable PacketChannel channel, @Nonnull T packet);

    /**
     * Registers a new generic {@link PacketHandler} for this network executor
     *
     * @param packetHandler the handler instance
     * @param <T> the generic packet
     */
    <T extends IPacket> void registerPacketHandler(@Nonnull PacketHandler<T> packetHandler);

    /**
     * Registers a single {@link PacketHandler} that will self-destruct (aka. unregister) itsself
     * after it has been fired one time
     *
     * @param packetHandler the handler instance
     * @param <T> the generic packet
     */
    <T extends IPacket> void registerSelfDestructivePacketHandler(@Nonnull PacketHandler<T> packetHandler);

    /**
     * Registers a self-destructive {@link PacketHandler} that unregisters itsself after handling
     * one time. But the given condition has to be true for it to function and unregister itsself.
     *
     * @param condition the condition to check
     * @param packetHandler the handler instance
     * @param <T> the generic packet
     * @see #registerSelfDestructivePacketHandler(PacketHandler)
     */
    <T extends IPacket> void registerConditionPacketHandler(@Nonnull BiSupplier<T, Boolean> condition, @Nonnull PacketHandler<T> packetHandler);

    /**
     * Unregisters a specific {@link PacketHandler} instance
     *
     * @param packetHandler the handler to unregister
     * @param <T> the generic of the handling packet
     */
    <T extends IPacket> void unregisterPacketHandler(@Nonnull PacketHandler<T> packetHandler);

    /**
     * Returns the current {@link PacketChannel} instance of this executor
     *
     * @see PacketChannel
     */
    PacketChannel getPacketChannel();


}
