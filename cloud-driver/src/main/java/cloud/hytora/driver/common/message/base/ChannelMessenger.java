package cloud.hytora.driver.common.message.base;

import cloud.hytora.driver.common.message.IMessageChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * The ChannelMessenger is used to register/unregister and manage all {@link IMessageChannel}s
 *
 * @see IMessageChannel
 * @since SNAPSHOT-1.5
 * @version STABLE-2.0
 * @author Lystx
 */
public interface ChannelMessenger {

    /**
     * Registers an {@link IMessageChannel} for a given generic type
     * under a provided specific name
     *
     * @param typeClass the generic type
     * @param name the name of the channel
     * @return the created instance
     */
    @NotNull
    <T> IMessageChannel<T> registerChannel(Class<T> typeClass, String name);

    /**
     * This method unregisters an {@link IMessageChannel} that was
     * registered under that name before
     * If there is no such channel under that name, nothing will happen!
     *
     * @param name the name of the channel
     */
    void unregisterChannel(String name);

    /**
     * Tries to retrieve a registered {@link IMessageChannel} by its
     * generic provided type class
     *
     * @param typeClass the class to match
     * @return the found channel or null
     */
    @Nullable
    <T> IMessageChannel<T> getRegisteredChannel(Class<T> typeClass);

    /**
     * Tries to retrieve a registered {@link IMessageChannel} by its
     * provided name
     *
     * @param channelName the name to match
     * @return the found channel or null
     */
    @Nullable
    <T> IMessageChannel<T> getRegisteredChannel(String channelName);

    /**
     * @return a {@link Collection} of all registered {@link IMessageChannel}
     */
    @NotNull
    Collection<IMessageChannel<?>> getRegisteredChannels();
}
