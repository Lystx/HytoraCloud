package cloud.hytora.driver.networking.protocol.wrapped;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.networking.AbstractHandlingNetworkExecutor;
import cloud.hytora.driver.networking.HandlingNetworkExecutor;
import cloud.hytora.driver.networking.packets.response.PacketResponse;
import cloud.hytora.driver.networking.protocol.SimpleNetworkComponent;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.IPacket;
import cloud.hytora.driver.networking.protocol.types.BufferState;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundInvoker;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import cloud.hytora.driver.networking.NetworkExecutor;
import cloud.hytora.driver.networking.packets.response.BufferedResponse;
import cloud.hytora.driver.networking.protocol.types.ConnectionState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor @Getter @NoArgsConstructor @Setter
public class SimplePacketChannel extends SimpleNetworkComponent implements PacketChannel {

    private UUID uniqueId;
    private boolean authenticated;

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
    public @NotNull PacketAction<BufferedResponse> sendQuery() {
        return new SimplePacketAction<>(this, BufferedResponse.class, "singleQuery");
    }

    @Override
    public @NotNull PacketAction<BufferedResponse> sendQuery(IPacket packet) {
        PacketAction<BufferedResponse> action = sendQuery();
        ((SimplePacketAction<BufferedResponse>)action).setPacket(packet);

        return action;
    }

    @Override
    public @NotNull PacketAction<Void> sendResponse(IPacket packet) {
        PacketAction<Void> action = sendResponse();
        ((SimplePacketAction<Void>)action).setPacket(packet);

        return action;
    }

    @Override
    public @NotNull PacketAction<Void> sendResponse() {
        return new SimplePacketAction<>(this, Void.class, "response");
    }

    @Override
    public boolean hasEverConnected() {
        return everConnected;
    }

    @Override
    public void flushPacket(IPacket packet) {
        if (!this.isActive()) {
            return;
        }
        ChannelOutboundInvoker invoker = this.wrapped.channel() == null ? this.wrapped : this.wrapped.channel();
        invoker.writeAndFlush(packet).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }


    @Override
    public void sendPacketSync(@Nonnull IPacket packet) {

        if (!this.isActive()) {
            return;
        }
        ChannelFuture future = writePacket(packet);
        if (future != null) {
            try {
                future.sync();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private ChannelFuture writePacket(@Nonnull IPacket packet) {
        if (!this.isActive()) {
            return null;
        }
        try {
            return this.wrapped.channel().writeAndFlush(packet);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public void close() {
        wrapped.channel().close();
    }


    @Override
    public boolean isActive() {
        return wrapped.channel().isActive();
    }

    @Override
    public boolean isWritable() {
        return wrapped.channel().isWritable();
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

    @NotNull
    public Task<BufferedResponse> registerQueryResponseHandler(@NotNull UUID uniqueId) {
        Task<BufferedResponse> task = Task.empty();
        if (executor() instanceof HandlingNetworkExecutor) {
            HandlingNetworkExecutor executor = (HandlingNetworkExecutor)executor();
            ((AbstractHandlingNetworkExecutor<?>)executor).registerQueryHandler(uniqueId, (wrapper, packet) -> {
                if (packet instanceof PacketResponse) {
                    PacketResponse response = (PacketResponse)packet;
                    task.setResult(response);
                }
            });
        }
        return task;
    }

    @NotNull
    @Override
    public Task<BufferedResponse> sendPacketQueryAsync(@NotNull IPacket packet) {
        Task<BufferedResponse> task = registerQueryResponseHandler(packet.transferInfo().getInternalQueryId());
        sendPacket(packet);
        return task;
    }

    @javax.annotation.Nullable
    @Override
    public BufferedResponse sendPacketQuery(@Nonnull IPacket packet) {
        return sendPacketQueryAsync(packet).timeOut(TimeUnit.SECONDS, 10).syncUninterruptedly().orElse(null);
    }




    @Override
    public void sendPacket(IPacket packet) {
        this.flushPacket(packet);
    }

    @Override
    public void log(String message, Object... args) {

    }

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
    }
}
