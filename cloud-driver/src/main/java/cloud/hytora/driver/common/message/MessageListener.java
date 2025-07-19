package cloud.hytora.driver.common.message;

/**
 * The {@link MessageListener} is used to listen to incoming objects that
 * are being handled within a {@link IMessageChannel}
 *
 * @param <T> the generic object type
 * @see IMessageChannel
 * @see cloud.hytora.driver.common.message.base.ChannelMessage
 *
 * @version STABLE-2.0
 * @since STABLE-2.0
 * @author Lystx
 */
public interface MessageListener<T> {

    /**
     * Handles an incoming object message
     *
     * @param startTime the time it was sent
     * @throws Exception if something goes wrong
     */
    void handleMessage(T t, long startTime) throws Exception;
}
