package cloud.hytora.driver.networking;

import cloud.hytora.common.collection.ThreadRunnable;
import cloud.hytora.driver.networking.protocol.SimpleNetworkComponent;
import cloud.hytora.driver.networking.protocol.packets.*;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public abstract class AbstractNetworkComponent<T extends AbstractNetworkComponent<T>> extends SimpleNetworkComponent implements AdvancedNetworkExecutor {

    /**
     * All cached packet handlers
     */
    protected final List<PacketHandler<?>> packetHandlers;

    /**
     * All cached packet handlers
     */
    protected final Map<String, List<PacketHandler<?>>> channelPacketHandlers;

    /**
     * Async options
     */
    protected boolean handlePacketsAsync, bootAsync;

    protected AbstractNetworkComponent(ConnectionType type, String name) {
        super(name, type);
        this.packetHandlers = new ArrayList<>();
        this.channelPacketHandlers = new HashMap<>();

        PacketProvider.registerPackets();
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
        this.registerChannelHandler("global_packet_channel", packetHandler);
    }

    @Override
    public void unregisterChannelHandlers(@NotNull String channelName) {
        this.channelPacketHandlers.remove(channelName);
    }

    @Override
    public <P extends IPacket> void registerSelfDestructivePacketHandler(@NotNull PacketHandler<P> packetHandler) {
        this.registerPacketHandler((PacketHandler<P>) (wrapper, packet) -> {
            ((AbstractPacket)packet).channel(wrapper);
            packetHandler.handle(wrapper, packet);
            unRegisterChannelHandler("global_packet_channel", packetHandler);
        });
    }

    @Override
    public <T extends IPacket> void unRegisterChannelHandler(@NotNull String channelName, @NotNull PacketHandler<T> packetHandler) {
        List<PacketHandler<?>> packetHandlers = this.channelPacketHandlers.get(channelName);
        if (packetHandlers != null) {
            packetHandlers.remove(packetHandler);
        }
    }

    @Override
    public <P extends IPacket> void registerChannelHandler(@NotNull String channelName, @NotNull PacketHandler<P> packetHandler) {
        List<PacketHandler<?>> packetHandlers = this.channelPacketHandlers.get(channelName);
        if (packetHandlers == null) {
            packetHandlers = new ArrayList<>();
            packetHandlers.add(packetHandler);
            this.channelPacketHandlers.put(channelName, packetHandlers);
        } else {
            packetHandlers.add(packetHandler);
        }
    }

    @Override
    public @NotNull List<PacketHandler<?>> getRegisteredPacketHandlers() {
        return this.packetHandlers;
    }

    @Override
    public <P extends IPacket> void handlePacket(PacketChannel wrapper, @NotNull P packet) {
        ThreadRunnable runnable = new ThreadRunnable(() -> {

            ((AbstractPacket)packet).channel(wrapper);
            List<PacketHandler<?>> packetHandlers = this.channelPacketHandlers.get(packet.getDestinationChannel());

            for (PacketHandler packetHandler : new ArrayList<>(packetHandlers)) {
                try {

                    packetHandler.handle(wrapper, packet);
                } catch (Exception e) {
                    if (e instanceof ClassCastException) {
                        //not right handler
                        continue;
                    }
                    e.printStackTrace();
                }
            }
    });

        if (handlePacketsAsync) {
            runnable.runAsync();
        } else {
            runnable.run();
        }
    }
}
