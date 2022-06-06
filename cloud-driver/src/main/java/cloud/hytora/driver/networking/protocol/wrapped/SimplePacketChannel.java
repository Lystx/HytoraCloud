package cloud.hytora.driver.networking.protocol.wrapped;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundInvoker;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import cloud.hytora.driver.networking.NetworkExecutor;
import cloud.hytora.driver.networking.protocol.packets.BufferedResponse;
import cloud.hytora.driver.networking.protocol.packets.ConnectionState;
import cloud.hytora.driver.networking.protocol.packets.Packet;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.Set;

@AllArgsConstructor @Getter @NoArgsConstructor @Setter
public class SimplePacketChannel implements PacketChannel {

    /**
     * The wrapped context
     */
    private ChannelHandlerContext wrapped;

    /**
     * The last time it got modified
     */
    private long modificationTime;

    /**
     * The participant
     */
    private NetworkExecutor participant;

    /**
     * The state of this context
     */
    private ConnectionState state;

    /**
     * If ever connected
     */
    private boolean everConnected;

    @Override
    public ConnectionState state() {
        return state;
    }

    @Override
    public NetworkExecutor executor() {
        return participant;
    }

    @Override
    public InetSocketAddress getClientAddress() {
        return wrapped.channel() == null ? InetSocketAddress.createUnresolved("127.0.0.1", 1234) : (InetSocketAddress) wrapped.channel().remoteAddress();
    }

    @Override
    public PacketChannel overrideExecutor(NetworkExecutor executor) {
        participant = executor;
        return this;
    }

    @Override
    public ChanneledPacketAction<Set<BufferedResponse>> prepareMultiQuery() {
        return new SimplePacketAction(this, Set.class, "multiQuery");
    }


    @Override
    public ChanneledPacketAction<BufferedResponse> prepareSingleQuery() {
        return new SimplePacketAction<>(this, BufferedResponse.class, "singleQuery");
    }

    @Override
    public ChanneledPacketAction<Void> prepareTransfer() {
        return new SimplePacketAction<>(this, Void.class, "transfer");
    }

    @Override
    public ChanneledPacketAction<Void> prepareResponse() {
        return new SimplePacketAction<>(this, Void.class, "response");
    }

    @Override
    public boolean hasEverConnected() {
        return everConnected;
    }

    @Override
    public void flushPacket(Packet packet) {
        ChannelOutboundInvoker invoker = this.wrapped.channel() == null ? this.wrapped : this.wrapped.channel();
        invoker.writeAndFlush(packet).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    @Override
    public void close() {
        wrapped.channel().close();
    }

    @Override
    public ChannelHandlerContext context() {
        return wrapped;
    }

    @Override
    public Optional<ChannelHandlerContext> optional() {
        return Optional.ofNullable(this.wrapped);
    }

    @Override
    public long modificationTime() {
        return modificationTime;
    }

    @Override
    public String toString() {
        return "[name=" + participant.getName() + ", type= " + participant.getType() + ", state=" + state + ", modificationTime=" + modificationTime + ", connected=" + everConnected + "]";
    }

    @Override
    public void sendPacket(Packet packet) {
        this.flushPacket(packet);
    }
}
