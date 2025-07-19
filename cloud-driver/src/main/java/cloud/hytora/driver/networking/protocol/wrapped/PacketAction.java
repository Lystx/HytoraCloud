package cloud.hytora.driver.networking.protocol.wrapped;

import cloud.hytora.common.task.Task;
import cloud.hytora.document.Document;
import cloud.hytora.driver.networking.packets.response.BufferedResponse;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.IPacket;
import cloud.hytora.driver.networking.packets.response.NetworkResponseState;
import cloud.hytora.driver.networking.protocol.types.ConnectionType;

import java.util.function.Consumer;

/**
 * A {@link PacketAction} can either be a query or a response.
 * For queries you need to be able to set the receivers/document/state/buffer etc etc.
 * Same for a response.
 *
 * @param <R> the generic result type
 * @version STABLE-1.0
 * @since STABLE-1.0
 * @see BufferedResponse
 */
public interface PacketAction<R> {

    /**
     * Sets the {@link NetworkResponseState} of this action
     *
     * @param state the state to set
     * @return current action instance
     */
    PacketAction<R> setState(NetworkResponseState state);

    /**
     * Sets the {@link Throwable} of this action
     *
     * @param error the state to set
     * @return current action instance
     */
    PacketAction<R> setError(Throwable error);

    /**
     * Sets the {@link Document} of this action
     *
     * @param document the document to set
     * @return current action instance
     */
    PacketAction<R> setDocument(Document document);
    PacketAction<R> setDocument(Consumer<Document> document);


    /**
     * Sets the {@link PacketBuffer} of this action
     *
     * @param buf the buf set
     * @return current action instance
     */
    PacketAction<R> setBuffer(PacketBuffer buf);
    PacketAction<R> setBuffer(Consumer<PacketBuffer> buf);

    /**
     * Sets the receivers of this action
     *
     * @param receivers the receivers
     * @return current action instance
     */
    PacketAction<R> setReceivers(String... receivers);
    PacketAction<R> setReceivers(ConnectionType... types);

    /**
     * Executes this action and returns
     * a {@link Task} that waits for the generic result
     *
     * @return task instance
     * @see Task
     */
    Task<R> execute(IPacket packet);

    /**
     * Executes this action and returns
     * a {@link Task} that waits for the generic result
     *
     * @return task instance
     * @see Task
     */
    Task<R> execute();

}
