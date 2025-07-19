package cloud.hytora.driver.networking;

import cloud.hytora.common.collection.ThreadRunnable;
import cloud.hytora.common.function.BiSupplier;
import cloud.hytora.common.function.ExceptionallyRunnable;
import cloud.hytora.driver.networking.packets.PacketRegistry;
import cloud.hytora.driver.networking.protocol.SimpleNetworkComponent;
import cloud.hytora.driver.networking.protocol.packets.*;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import cloud.hytora.driver.networking.protocol.types.ConnectionType;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Getter
public abstract class AbstractHandlingNetworkExecutor<T extends AbstractHandlingNetworkExecutor<T>> extends SimpleNetworkComponent implements HandlingNetworkExecutor {

    /**
     * All cached packet handlers
     */
    protected final Collection<Callback> registeredPacketHandlers;

    /**
     * Waiting packets
     */
    private final Map<UUID, Callback> waitingPackets;

    /**
     * Async options
     */
    protected boolean handlePacketsAsync, bootAsync;

    protected AbstractHandlingNetworkExecutor(ConnectionType type, String name) {
        super(name, type);
        this.registeredPacketHandlers = new ArrayList<>();
        this.waitingPackets = new ConcurrentHashMap<>();

        this.handlePacketsAsync = false;
        PacketRegistry.registerPackets();
    }

    public T handlePacketsAsync() {
        this.handlePacketsAsync = true;
        return (T) this;
    }

    public T bootAsync() {
        this.bootAsync = true;
        return (T) this;
    }

    @Override
    public <E extends IPacket> void registerPacketHandler(@NotNull PacketHandler<E> packetHandler) {
        this.registeredPacketHandlers.add(new Callback((wrapper, packet) -> packetHandler.handle(wrapper, (E) packet)));
    }



    public <T extends IPacket> void registerQueryHandler(@NotNull UUID uniqueId, @NotNull PacketHandler<T> packetHandler) {

        Callback callback = new Callback((wrapper, packet) -> packetHandler.handle(wrapper, (T) packet));
        callback.setUniqueId(uniqueId);
        callback.setAutoRemove(true);

        this.waitingPackets.put(uniqueId, callback);
    }

    @Override
    public <T extends IPacket> void unregisterPacketHandler(@NotNull PacketHandler<T> packetHandler) {
        this.registeredPacketHandlers.removeIf(callback -> packetHandler.equals(callback.getHandler()));
    }

    @Override
    public <P extends IPacket> void registerSelfDestructivePacketHandler(@NotNull PacketHandler<P> packetHandler) {
        Callback callback = new Callback((wrapper, packet) -> packetHandler.handle(wrapper, (P) packet));
        callback.setAutoRemove(true);
        this.registeredPacketHandlers.add(callback);
    }

    @Override
    public <P extends IPacket> void registerConditionPacketHandler(@NotNull BiSupplier<P, Boolean> condition, @NotNull PacketHandler<P> packetHandler) {
        Callback callback = new Callback((wrapper, packet) -> packetHandler.handle(wrapper, (P) packet));
        callback.setCondition(packet -> condition.supply((P) packet));
        callback.setAutoRemove(true);
        this.registeredPacketHandlers.add(callback);
    }


    @Override
    public <P extends IPacket> void handlePacket(PacketChannel channel, @NotNull P packet) {
        ((AbstractPacket) packet).channel(channel);

        ExceptionallyRunnable runnable = () -> {

            //checking for query handler
            if (waitingPackets.containsKey(packet.transferInfo().getInternalQueryId())) {
                Callback callback = null;
                try {
                    callback = waitingPackets.get(packet.transferInfo().getInternalQueryId());
                    callback.handler.handle(channel, packet);
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
                if (handleCallback(packet, callback)) {
                    waitingPackets.remove(packet.transferInfo().getInternalQueryId());
                }
            }

            for (Callback callback : new ArrayList<>(this.registeredPacketHandlers)) {
                if (callback == null || callback.getHandler() == null) {
                    continue;
                }
                PacketHandler<? super IPacket> handler = callback.getHandler();
                try {
                    handler.handle(channel, packet);
                } catch (Exception e) {
                    if (e instanceof ClassCastException) {
                        //not right handler -> ignore and skip handler
                        continue;
                    }
                    e.printStackTrace();
                }
                if (handleCallback(packet, callback)) {
                    this.registeredPacketHandlers.remove(callback);
                }
            }
        };
        if (handlePacketsAsync) {
            new ThreadRunnable(runnable).runAsync();
        } else {
            runnable.run();
        }
    }

    private boolean handleCallback(IPacket packet, Callback callback) {

        if (callback != null && callback.autoRemove) {
            if (callback.getCondition() == null && callback.getBiCondition() == null) {
                return true;
            } else if (callback.getCondition() == null && callback.getBiCondition() != null) {
                return callback.getBiCondition().supply(packet);
            } else if (callback.getCondition() != null && callback.getBiCondition() == null) {
                return callback.getCondition().get();
            }
            return callback.getCondition().get() && callback.getBiCondition().supply(packet);
        }
        return false;
    }

    @Getter
    @Setter
    private static final class Callback {

        private final PacketHandler<? super IPacket> handler;

        private UUID uniqueId;
        private boolean autoRemove;
        private Supplier<Boolean> condition;
        private BiSupplier<? super IPacket, Boolean> biCondition;

        public Callback(PacketHandler<? super IPacket> handler) {
            this.handler = handler;
            this.autoRemove = false;
            this.uniqueId = UUID.randomUUID();
        }

        public void setCondition(BiSupplier<? super IPacket, Boolean> biCondition) {
            this.biCondition = biCondition;
        }
    }
}
