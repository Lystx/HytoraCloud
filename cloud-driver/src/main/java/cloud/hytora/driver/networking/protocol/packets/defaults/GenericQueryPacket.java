package cloud.hytora.driver.networking.protocol.packets.defaults;

import cloud.hytora.common.scheduler.Scheduler;
import cloud.hytora.common.task.ITask;
import cloud.hytora.document.Document;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.codec.buf.defaults.*;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

@Setter
@NoArgsConstructor
public class GenericQueryPacket<T extends IBufferObject> extends AbstractPacket {

    /**
     * The query id for identification
     */
    @Getter
    private UUID queryId;

    /**
     * The key for request
     */
    @Getter
    private String key;

    /**
     * The request object
     */
    @Getter
    private IBufferObject request;

    /**
     * The request type class
     */
    private Class requestTypeClass;

    /**
     * The result type class
     */
    private Class responseTypeClass;

    /**
     * If query or response
     */
    private boolean querySide;

    /**
     * The result that was responded with
     */
    @Getter
    private T result;

    public GenericQueryPacket(@NotNull String key,@NotNull IBufferObject request) {
        this.request = request;
        this.key = key;
        this.requestTypeClass = request.getClass();
        this.queryId = UUID.randomUUID();
    }

    public ITask<GenericQueryPacket<T>> query() {
        ITask<GenericQueryPacket<T>> task = ITask.empty();

        //mark this is query side
        this.querySide = true;

        //publish async and wait for finish
        super.publishAsync().onTaskSucess(v -> {

            CloudDriver
                    .getInstance()
                    .getNetworkExecutor()
                    .registerPacketHandler((PacketHandler<GenericQueryPacket>) (wrapper, packet) -> {
                        if (packet.getQueryId().equals(queryId)) {
                            task.setResult(packet);
                        }
                    });

        });

        return task;
    }

    public ITask<Void> respond(String result) {
        return this.respond0(new BufferedString(result));
    }

    public ITask<Void> respond(boolean result) {
        return this.respond0(new BufferedBoolean(result));
    }

    public ITask<Void> respond(Document result) {
        return this.respond0(new BufferedDocument(result));
    }

    public ITask<Void> respond(Enum<?> result) {
        return this.respond0(new BufferedEnum(result));
    }

    public ITask<Void> respond(T result) {
        return this.respond0(new BufferedCustom<>(result));
    }

    public ITask<Void> respond(Number result) {
        if (result instanceof Integer) {
            return this.respond0(new BufferedInt((Integer) result));
        }
        if (result instanceof Long) {
            return this.respond0(new BufferedLong((Long) result));
        }
        if (result instanceof Byte) {
            return this.respond0(new BufferedByte((Byte) result));
        }
        if (result instanceof Double) {
            return this.respond0(new BufferedDouble((Double) result));
        }
        return ITask.empty();
    }

    private ITask<Void> respond0(IBufferObject data) {
        this.result = (T) data;
        this.querySide = false;
        return this.publishAsync();
    }


    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        switch (state) {
            case WRITE:
                buf.writeUniqueId(queryId);
                buf.writeString(key);
                buf.writeBoolean(querySide);
                if (querySide) {
                    buf.writeClass(requestTypeClass);
                    buf.writeOptionalObject(request);
                } else {
                    buf.writeClass(responseTypeClass);
                    buf.writeOptionalObject(result);
                }
                break;
            case READ:
                queryId = buf.readUniqueId();
                key = buf.readString();
                boolean querySide = buf.readBoolean();
                if (querySide) {
                    requestTypeClass = buf.readClass();
                    request = buf.readOptionalObject(requestTypeClass);
                } else {
                    responseTypeClass = buf.readClass();
                    result = (T) buf.readOptionalObject(responseTypeClass);
                }
                break;
        }
    }

    private static Map<String, Collection<Consumer<GenericQueryPacket<?>>>> listeners;

    static {
        listeners = new HashMap<>();


        Scheduler.runTimeScheduler().executeIf(() -> {

            CloudDriver
                    .getInstance()
                    .getNetworkExecutor()
                    .registerPacketHandler(new PacketHandler<GenericQueryPacket>() {
                        @Override
                        public void handle(PacketChannel wrapper, GenericQueryPacket packet) {

                            Collection<Consumer<GenericQueryPacket<?>>> consumers = listeners.get(packet.getKey());
                            if (consumers != null) {
                                for (Consumer<GenericQueryPacket<?>> consumer : consumers) {
                                    consumer.accept(packet);
                                }
                            }
                        }
                    });
        }, () -> CloudDriver.getInstance() != null && CloudDriver.getInstance().getNetworkExecutor() != null);
    }

    @SuppressWarnings("unchecked")
    public static <T extends IBufferObject> void listenFor(String key, Class<T> typeClass, Consumer<GenericQueryPacket<T>> handler) {
        Collection<Consumer<GenericQueryPacket<?>>> consumers = listeners.get(key);
        if (consumers == null) {
            consumers = new ArrayList<>();
        }
        consumers.add(wrapped -> handler.accept((GenericQueryPacket<T>) wrapped));
        listeners.put(key, consumers);
    }
}
