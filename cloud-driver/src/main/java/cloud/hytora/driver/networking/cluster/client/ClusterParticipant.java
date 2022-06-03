package cloud.hytora.driver.networking.cluster.client;

import cloud.hytora.common.collection.ThreadRunnable;
import cloud.hytora.common.wrapper.Wrapper;
import cloud.hytora.document.Document;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.event.defaults.driver.DriverConnectEvent;
import cloud.hytora.driver.networking.protocol.codec.NetworkBossHandler;
import cloud.hytora.driver.networking.protocol.codec.PacketDecoder;
import cloud.hytora.driver.networking.protocol.codec.PacketEncoder;
import cloud.hytora.driver.networking.protocol.codec.prepender.NettyPacketLengthDeserializer;
import cloud.hytora.driver.networking.protocol.codec.prepender.NettyPacketLengthSerializer;
import cloud.hytora.driver.networking.protocol.packets.ConnectionType;
import cloud.hytora.driver.networking.protocol.packets.IPacket;
import cloud.hytora.driver.networking.protocol.packets.Packet;
import cloud.hytora.driver.networking.protocol.packets.defaults.HandshakePacket;
import cloud.hytora.driver.networking.protocol.wrapped.ChannelWrapper;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import cloud.hytora.driver.networking.AbstractNetworkComponent;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.nio.channels.AlreadyConnectedException;

@Getter
public abstract class ClusterParticipant extends AbstractNetworkComponent<ClusterParticipant> {

    private MultithreadEventLoopGroup workerGroup;
    private boolean active;
    private Channel channel;
    private Document customData;
    private String connectedNodeName;

    public ClusterParticipant(String clientName, ConnectionType type, Document customData) {
        super(type, clientName);

        this.active = false;
        this.channel = null;
        this.customData = customData;
        this.connectedNodeName = "UNKNOWN";
    }

    @Override
    public ChannelWrapper getWrapper() {
        return channel.pipeline().get(NetworkBossHandler.class).getWrapper();
    }


    public Wrapper<Channel> openConnection(String hostname, int port) {
        Wrapper<Channel> result = Wrapper.empty(Channel.class).denyNull();

        if (active) {
            result.setFailure(new AlreadyConnectedException());
            return result;
        }

        this.active = true;
        this.workerGroup = Epoll.isAvailable() ? new EpollEventLoopGroup() : new NioEventLoopGroup();

        ThreadRunnable runnable = new ThreadRunnable(() -> {

            new Bootstrap()
                    .channel(Epoll.isAvailable() ? EpollSocketChannel.class : NioSocketChannel.class)
                    .group(workerGroup)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) {

                            channel.pipeline()
                                    .addLast(new NettyPacketLengthDeserializer())
                                    .addLast(new PacketDecoder(ClusterParticipant.this))
                                    .addLast(new NettyPacketLengthSerializer())
                                    .addLast(new PacketEncoder(ClusterParticipant.this))
                                    .addLast(new NetworkBossHandler(ClusterParticipant.this) {

                                        @Override
                                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                            ClusterParticipant.this.onActivated(ctx);
                                            ClusterParticipant.this.sendPacket(new HandshakePacket(getName(), ClusterParticipant.this.type, customData));
                                            result.setResult(ctx.channel());

                                            //fire connect event
                                            CloudDriver.getInstance().getEventManager().callEvent(new DriverConnectEvent());

                                            super.channelActive(ctx);
                                        }

                                        @Override
                                        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                            CloudDriver.getInstance().getEventManager().callEvent(new DriverConnectEvent());
                                            onClose(ctx);
                                            super.channelInactive(ctx);
                                        }

                                        @Override
                                        public void channelRead0(ChannelHandlerContext channelHandlerContext, IPacket packet) {
                                            super.channelRead0(channelHandlerContext, packet);
                                        }
                                    });
                        }
                    })
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .connect(hostname, port).addListener((ChannelFutureListener) future -> {
                        if (future.isSuccess()) {
                            channel = future.channel();
                        } else {
                            result.setFailure(future.cause());
                            workerGroup.shutdownGracefully();
                        }
                    });
        });

        if (this.bootAsync) {
            runnable.runAsync();
        } else {
            runnable.run();
        }
        return result;
    }

    @Override
    public <T extends IPacket> void handlePacket(ChannelWrapper wrapper, @NotNull T packet) {
        if (packet instanceof HandshakePacket) {
            HandshakePacket handshake = (HandshakePacket) packet;
            connectedNodeName = handshake.getNodeName();

            ThreadRunnable runnable = new ThreadRunnable(() -> onAuthenticationChanged(wrapper));
            if (handlePacketsAsync) {
                runnable.runAsync();
            } else {
                runnable.run();
            }
            return;
        }
        super.handlePacket(wrapper, packet);
    }


    public Wrapper<Boolean> shutdown() {
        Wrapper<Boolean> wrapper = Wrapper.empty();
        this.workerGroup.shutdownGracefully().addListener(future -> {
            if (future.isSuccess()) {
                wrapper.setResult(true);
            } else {
                wrapper.setFailure(future.cause());
            }
        });
        return wrapper;
    }

    @Override
    public void sendPacket(IPacket packet) {
        this.channel.writeAndFlush(packet).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    public abstract void onAuthenticationChanged(ChannelWrapper wrapper);

    public abstract void onActivated(ChannelHandlerContext ctx);

    public abstract void onClose(ChannelHandlerContext ctx);

}
