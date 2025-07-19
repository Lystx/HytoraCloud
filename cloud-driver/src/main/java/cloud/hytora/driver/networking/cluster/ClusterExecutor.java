package cloud.hytora.driver.networking.cluster;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.PublishingType;
import cloud.hytora.driver.event.defaults.driver.CloudEventDriverConnect;
import cloud.hytora.driver.event.defaults.driver.CloudEventDriverDisconnect;
import cloud.hytora.driver.networking.EndpointNetworkExecutor;
import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.networking.protocol.codec.NetworkBossHandler;
import cloud.hytora.driver.networking.protocol.codec.PacketDecoder;
import cloud.hytora.driver.networking.protocol.codec.PacketEncoder;
import cloud.hytora.driver.networking.protocol.codec.prepender.NettyPacketLengthDeserializer;
import cloud.hytora.driver.networking.protocol.codec.prepender.NettyPacketLengthSerializer;
import cloud.hytora.driver.networking.protocol.types.ConnectionState;
import cloud.hytora.driver.networking.protocol.types.ConnectionType;

import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import cloud.hytora.driver.networking.protocol.packets.IPacket;
import cloud.hytora.driver.networking.packets.auth.PacketHandshake;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.networking.protocol.wrapped.SimplePacketChannel;
import cloud.hytora.driver.networking.AbstractHandlingNetworkExecutor;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Getter
@Setter
public abstract class ClusterExecutor extends AbstractHandlingNetworkExecutor<ClusterExecutor> implements EndpointNetworkExecutor {

    /**
     * The name of this node
     */
    private final String nodeName;

    /**
     * Authentication key
     */
    private final String authKey;

    /**
     * ALl the connected channels stored by their identifier
     */
    private final Map<UUID, PacketChannel> connectedChannels;

    //netty stuff
    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;
    private EventExecutorGroup eventExecutorGroup;
    private SimplePacketChannel packetChannel;

    public ClusterExecutor(String authKey, String nodeName) {
        super(ConnectionType.NODE, nodeName);

        this.authKey = authKey;
        this.nodeName = nodeName;

        this.connectedChannels = new HashMap<>();

        this.packetChannel = new SimplePacketChannel();
        this.packetChannel.setAuthenticated(true);
        this.packetChannel.setUniqueId(UUID.randomUUID());
        this.packetChannel.setName(nodeName);
        this.packetChannel.setType(ConnectionType.NODE);

        this.packetChannel.setState(ConnectionState.DISCONNECTED);
        this.packetChannel.setModificationTime(System.currentTimeMillis());
        this.packetChannel.setParticipant(this);
        this.packetChannel.setEverConnected(false);
        this.packetChannel.setWrapped(null);
    }


    public Task<EndpointNetworkExecutor> openConnection(String hostname, int port) {
        Task<EndpointNetworkExecutor> connectPromise = Task.empty();
        connectPromise.denyNull();


        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();

        this.eventExecutorGroup = new DefaultEventExecutorGroup(20);

        new ServerBootstrap()
                .group(this.bossGroup, this.workerGroup)
                .channel(Epoll.isAvailable() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
                        socketChannel.pipeline()
                                .addLast(new NettyPacketLengthDeserializer())
                                .addLast(new PacketDecoder(ClusterExecutor.this))
                                .addLast(new NettyPacketLengthSerializer())
                                .addLast(new PacketEncoder(ClusterExecutor.this))
                                .addLast(new NetworkBossHandler(ClusterExecutor.this) {

                                             @Override
                                             public void channelActive(ChannelHandlerContext ctx) {
                                                 SimplePacketChannel packetChannel = new SimplePacketChannel();

                                                 packetChannel.setName("NOT_RESOLVED_YET");
                                                 packetChannel.setUniqueId(UUID.randomUUID());
                                                 packetChannel.setType(ConnectionType.SERVICE);
                                                 packetChannel.setAuthenticated(false);
                                                 packetChannel.setParticipant(packetChannel);
                                                 packetChannel.setWrapped(ctx);
                                                 packetChannel.setModificationTime(System.currentTimeMillis());
                                                 packetChannel.setState(ConnectionState.CONNECTED);
                                                 packetChannel.setEverConnected(true);

                                                 connectedChannels.put(packetChannel.getUniqueId(), packetChannel);
                                             }

                                             @Override
                                             public void channelRead0(ChannelHandlerContext channelHandlerContext, AbstractPacket packet) {

                                                 PacketChannel channel = getConnectedChannel(channelHandlerContext.channel());


                                                 if (channel == null) {
                                                     CloudDriver.getInstance().getLogger().error("Tried to read Packet from unknown channel that has not been registered before! Closing channel for safety reasons...");
                                                     channelHandlerContext.close();
                                                     return;
                                                 }

                                                 if (!channel.isAuthenticated()) {

                                                     if (packet instanceof PacketHandshake) {
                                                         PacketHandshake authPacket = (PacketHandshake) packet;

                                                         channel.setName(authPacket.getClientName());

                                                         if (!authPacket.getAuthKey().equalsIgnoreCase(authKey)) {

                                                             CloudDriver.getInstance().getLogger().error(" §8 ");
                                                             CloudDriver.getInstance().getLogger().error(" §8<=====[§4ERROR§8]=====>");
                                                             CloudDriver.getInstance().getLogger().error(" §cTried to authenticate §e{} §cbut wrong AuthKey was provided", channel.getName());
                                                             CloudDriver.getInstance().getLogger().error(" §cClosing channel...");
                                                             CloudDriver.getInstance().getLogger().error(" §8<=====[§4ERROR§8]=====>");
                                                             CloudDriver.getInstance().getLogger().error(" §8 ");
                                                             channelHandlerContext.close();
                                                             return;
                                                         }
                                                         channel.setAuthenticated(true);
                                                         channel.setType(authPacket.getType());

                                                         //channel.setData(authPacket.getExtraData());

                                                         //setting node name and sending back
                                                         authPacket.setNodeName(getNodeName());
                                                         channel.sendPacket(authPacket);

                                                         //updating value in cache
                                                         connectedChannels.put(channel.getUniqueId(), channel);

                                                         handleConnectionChange(ConnectionState.CONNECTED, channel);
                                                     } else {
                                                         CloudDriver.getInstance().getLogger().error(" §8 ");
                                                         CloudDriver.getInstance().getLogger().error(" §8<=====[§4ERROR§8]=====>");
                                                         CloudDriver.getInstance().getLogger().error(" §cTried to authenticate §e{} §cbut the first Packet was {} instead of {}", channel.getName(), packet.getClass().getSimpleName(), PacketHandshake.class.getSimpleName());
                                                         CloudDriver.getInstance().getLogger().error(" §cClosing channel...");
                                                         CloudDriver.getInstance().getLogger().error(" §8<=====[§4ERROR§8]=====>");
                                                         CloudDriver.getInstance().getLogger().error(" §8 ");
                                                         channelHandlerContext.close();
                                                     }
                                                     return;
                                                 }
                                                 handlePacket(channel, packet);
                                             }

                                             @Override
                                             public void channelInactive(final ChannelHandlerContext ctx) {
                                                 closeClient(ctx);
                                             }

                                             @Override
                                             public void channelUnregistered(final ChannelHandlerContext ctx) {
                                                 closeClient(ctx);
                                             }
                                         }
                                );
                    }
                }).option(ChannelOption.SO_BACKLOG, 128)
                .bind(hostname, port).addListener(future -> {
                    this.packetChannel.setEverConnected(true);
                    this.packetChannel.setState(ConnectionState.CONNECTED);
                    CloudDriver.getInstance().getEventManager().callEvent(new CloudEventDriverConnect(), PublishingType.INTERNAL);
                    if (future.isSuccess()) {
                        connectPromise.setResult(this);
                    } else {
                        connectPromise.setFailure(future.cause());
                    }
                });

        return connectPromise;
    }

    public Task<Boolean> shutdown() {
        Task<Boolean> shutdownBossPromise = Task.empty();
        Task<Boolean> shutdownWorkerPromise = Task.empty();
        Task<Boolean> executePromise = Task.empty();

        CloudDriver.getInstance().getEventManager().callEvent(new CloudEventDriverDisconnect(), PublishingType.INTERNAL);
        Task<Boolean> promise = Task.multiTasking(shutdownBossPromise, shutdownBossPromise, executePromise);

        this.bossGroup.shutdownGracefully(0, 1, TimeUnit.MINUTES).addListener(it -> shutdownBossPromise.setResult(true));
        this.workerGroup.shutdownGracefully(0, 1, TimeUnit.MINUTES).addListener(it -> shutdownWorkerPromise.setResult(true));
        this.eventExecutorGroup.shutdownGracefully(0, 1, TimeUnit.MINUTES).addListener(it -> executePromise.setResult(true));

        return promise;
    }


    @Override
    public void sendPacket(IPacket packet) {
        this.sendPacketToAll(packet);
    }

    @Override
    public String getName() {
        return nodeName;
    }

    @Override
    public ConnectionType getType() {
        return ConnectionType.NODE;
    }

    public Collection<PacketChannel> getConnectedChannels() {
        return connectedChannels.values();
    }


    @Override
    public PacketChannel getConnectedChannel(Channel channel) {
        return getConnectedChannels().stream().filter(c -> c.context().channel() == channel).findFirst().orElse(null);
    }

    public void closeClient(ChannelHandlerContext context) {
        PacketChannel connectedChannel = getConnectedChannel(context.channel());
        if (connectedChannel == null) {
            return;
        }
        this.handleConnectionChange(ConnectionState.DISCONNECTED, connectedChannel);

        this.connectedChannels.remove(connectedChannel.getUniqueId());
    }


    public @NotNull Collection<PacketChannel> getAllConnectedChannels() {
        return getConnectedChannels();
    }

    public @NotNull Collection<PacketChannel> getConnectedChannels(ConnectionType type) {
        return this.getConnectedChannels().stream().filter(it -> it.getType().equals(type)).collect(Collectors.toList());
    }


    @Override
    public PacketChannel getConnectedChannel(String name) {
        return this.getConnectedChannels().stream().filter(client -> client.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    @Override
    public PacketChannel getConnectedChannel(UUID uniqueId) {
        return this.getConnectedChannels().stream().filter(client -> client.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    @Override
    public void sendPacket(IPacket packet, NetworkComponent component) {
        String name = component.getName();
        PacketChannel connectedChannel = getConnectedChannel(name);
        if (connectedChannel != null) {
            connectedChannel.sendPacket(packet);
        }
    }


    public void sendPacketToType(AbstractPacket packet, ConnectionType type) {
        this.getConnectedChannels(type).forEach(it -> it.sendPacket(packet));
    }


    public void sendPacketToAll(IPacket packet) {
        for (PacketChannel connectedChannel : new ArrayList<>(getConnectedChannels())) {
            connectedChannel.sendPacket(packet);
        }
    }

    public abstract void handleConnectionChange(ConnectionState state, PacketChannel channel);


}
