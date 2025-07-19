package cloud.hytora.driver.networking.cluster.client;

import cloud.hytora.common.collection.ThreadRunnable;
import cloud.hytora.common.misc.Util;
import cloud.hytora.common.task.Task;
import cloud.hytora.common.task.TaskResult;
import cloud.hytora.common.task.TaskState;
import cloud.hytora.document.Document;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.PublishingType;
import cloud.hytora.driver.event.defaults.driver.CloudEventDriverConnect;
import cloud.hytora.driver.networking.HandlingNetworkExecutor;
import cloud.hytora.driver.networking.protocol.ProtocolAddress;
import cloud.hytora.driver.networking.protocol.codec.NetworkBossHandler;
import cloud.hytora.driver.networking.protocol.codec.PacketDecoder;
import cloud.hytora.driver.networking.protocol.codec.PacketEncoder;
import cloud.hytora.driver.networking.protocol.codec.prepender.NettyPacketLengthDeserializer;
import cloud.hytora.driver.networking.protocol.codec.prepender.NettyPacketLengthSerializer;
import cloud.hytora.driver.networking.protocol.types.ConnectionType;
import cloud.hytora.driver.networking.protocol.packets.IPacket;
import cloud.hytora.driver.networking.packets.auth.PacketHandshake;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import cloud.hytora.driver.networking.AbstractHandlingNetworkExecutor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.nio.channels.AlreadyConnectedException;

@Getter
public abstract class ClusterParticipant extends AbstractHandlingNetworkExecutor<ClusterParticipant> {

    private MultithreadEventLoopGroup workerGroup;
    private boolean active;
    private Channel channel;
    private Document customData;
    private String connectedNodeName;

    @Setter
    private String authKey;

    public ClusterParticipant(String authKey, String clientName, ConnectionType type, Document customData) {
        super(type, clientName);

        this.authKey = authKey;
        this.active = false;
        this.channel = null;
        this.customData = customData;
        this.connectedNodeName = "UNKNOWN";
    }

    @Override
    public PacketChannel getPacketChannel() {
        return channel.pipeline().get(NetworkBossHandler.class).getPacketChannel();
    }


    public Task<TaskResult<Channel>> openConnection(ProtocolAddress address) {
        return openConnection(address.getHost(), address.getPort());
    }
    public Task<TaskResult<Channel>> openConnection(String hostname, int port, Runnable... handlers) {
        Task<TaskResult<Channel>> result = Task.empty();

        if (active) {
            result.setResult(new TaskResult<>(TaskState.ERROR, new AlreadyConnectedException()));
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
                                            if (ClusterParticipant.this instanceof AdvancedClusterParticipant) {
                                                ((AdvancedClusterParticipant)ClusterParticipant.this).onActivated(ctx);
                                            }
                                            //ClusterParticipant.this.onActivated(ctx);
                                            ClusterParticipant.this.sendPacket(new PacketHandshake(authKey, getName(), ClusterParticipant.this.type, customData));

                                            //fire connect event
                                            CloudDriver.getInstance().getEventManager().callEvent(new CloudEventDriverConnect(), PublishingType.INTERNAL);
                                            super.channelActive(ctx);
                                            for (Runnable handler : handlers) {
                                                handler.run();
                                            }

                                        }

                                        @Override
                                        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                            //onClose(ctx);
                                            if (ClusterParticipant.this instanceof AdvancedClusterParticipant) {
                                                ((AdvancedClusterParticipant)ClusterParticipant.this).onClose(ctx);
                                            }
                                            super.channelInactive(ctx);
                                        }

                                    });
                        }
                    })
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .connect(hostname, port).addListener((ChannelFutureListener) future -> {
                        if (future.isSuccess()) {
                            channel = future.channel();
                            if (channel == null) {
                                result.setResult(new TaskResult<>(TaskState.NULL));
                                return;
                            }
                            result.setResult(new TaskResult<>(TaskState.SUCCESS, channel));
                        } else {
                            result.setResult(new TaskResult<>(TaskState.ERROR, future.cause()));
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
    public <T extends IPacket> void handlePacket(PacketChannel channel, @NotNull T packet) {
        if (packet instanceof PacketHandshake) {
            PacketHandshake handshake = (PacketHandshake) packet;
            connectedNodeName = handshake.getNodeName();

            ThreadRunnable runnable = new ThreadRunnable(() -> {
                if (ClusterParticipant.this instanceof AdvancedClusterParticipant) {
                    CloudDriver.getInstance().getLogger().trace("Auhentication has changed for this NetworkParticipant.");
                    ((AdvancedClusterParticipant)ClusterParticipant.this).onAuthenticationChanged(channel);
                } else {
                    CloudDriver.getInstance().getLogger().trace("Tried to read Handshake but the NetworkParticipant is not a {} but a {}.", HandlingNetworkExecutor.class.getName(), getClass().getSuperclass().getName());
                }
            });
            if (handlePacketsAsync) {
                runnable.runAsync();
            } else {
                runnable.run();
            }
            return;
        }
        super.handlePacket(channel, packet);
    }


    public Task<Boolean> shutdown() {
        Task<Boolean> task = Task.empty();
        this.workerGroup.shutdownGracefully().addListener(future -> {
            if (future.isSuccess()) {
                task.setResult(true);
            } else {
                task.setFailure(future.cause());
            }
        });
        return task;
    }

    @Override
    public void sendPacket(IPacket packet) {
        if (this.channel == null) {
            //re-schedule request

            Util.executeIf(() -> sendPacket(packet), () -> getChannel() != null);
            return;
        }
        this.getChannel().writeAndFlush(packet).addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) future.cause().printStackTrace();
        });
    }

}
