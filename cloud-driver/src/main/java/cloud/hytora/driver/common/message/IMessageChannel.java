package cloud.hytora.driver.common.message;

import cloud.hytora.common.Snowflaked;
import cloud.hytora.common.identification.ImmutableNameHolder;
import cloud.hytora.driver.common.message.base.ChannelMessage;
import cloud.hytora.driver.networking.NetworkComponent;

/**
 * The {@link IMessageChannel} is used to send generic objects across the network
 * to other participants that have registered the same channel.
 *
 * There are two types of usage:
 *
 *  1. Use the built-in {@link ChannelMessage} as a generic. This is way more efficient
 *  2. Use any Object of your choice. This will be serialized/deserialized using gson
 *     and is not as efficient as method 1.
 *
 * @version STABLE-2.0
 * @since SNAPSHOT-1.5
 * @author Lystx
 * @param <T> the generic type of objects handled in this channel
 *
 * @see ChannelMessage
 * @see Snowflaked
 */
public interface IMessageChannel<T> extends ImmutableNameHolder, Snowflaked {

    /**
     * The generic type class of this channel
     */
    Class<?> getTypeClass();

    /**
     * Sends an object from this channel
     * to a given {@link NetworkComponent} to handle the message
     *
     * @param t the object
     * @param receiver the receiver
     */
    void sendMessage(T t, NetworkComponent... receiver);

    /**
     * Sends an object from this channel to all instances
     * that are able to receive Packets and Messages
     *
     * @param t the object
     */
    void sendMessage(T t);

    /**
     * Registers a {@link MessageListener} for this channel
     * to handle incoming objects
     *
     * @param listener the listener
     */
    void registerListener(MessageListener<T> listener);


    /**
     * Unregisters all listeners
     * and this channel
     */
    void unregister();
}
