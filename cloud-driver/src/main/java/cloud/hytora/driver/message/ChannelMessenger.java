package cloud.hytora.driver.message;

import cloud.hytora.driver.networking.NetworkComponent;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface ChannelMessenger {


    /**
     * Registers listener for a given channel
     *
     * @param channel the channel
     * @param consumer the listener as consumer
     */
    void registerChannel(String channel, ChannelMessageListener consumer);

    <T extends DocumentPacket> void registerPacketChannel(String channel, Consumer<T> handler);

    /**
     * Unregisters a channel to listen for
     *
     * @param channel the channel
     */
    void unregisterChannel(String channel);

    /**
     * Sends a {@link ChannelMessage} to the receiver of it
     *
     * @param message the message
     */
    default void sendChannelMessage(ChannelMessage message) {
        this.sendChannelMessage(message, message.getReceivers());
    }

    void sendDocumentPacket(DocumentPacket packet);

    void sendDocumentPacket(DocumentPacket packet, NetworkComponent[] receivers);

    /**
     * Sends a {@link ChannelMessage} to a given {@link NetworkComponent} receiver
     *
     * @param message the message
     * @param receivers the receivers
     */
    void sendChannelMessage(ChannelMessage message, NetworkComponent[] receivers);

    /**
     * Gets a list of all registered channels
     *
     * @return list of channels
     */
    default List<String> getChannel() {
        return new LinkedList<>(getCache().keySet());
    }

    /**
     * The whole cache for channel and listeners
     *
     * @return cache
     */
    Map<String, ChannelMessageListener> getCache();
}
