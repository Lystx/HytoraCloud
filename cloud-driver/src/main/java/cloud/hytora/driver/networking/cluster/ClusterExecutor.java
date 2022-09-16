package cloud.hytora.driver.networking.cluster;

import cloud.hytora.common.misc.StringUtils;
import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.event.IEventManager;
import cloud.hytora.driver.event.defaults.driver.DriverConnectEvent;
import cloud.hytora.driver.event.defaults.driver.DriverDisconnectEvent;
import cloud.hytora.driver.networking.EndpointNetworkExecutor;
import cloud.hytora.driver.networking.cluster.client.SimpleClusterClientExecutor;
import cloud.hytora.driver.networking.protocol.codec.NetworkBossHandler;
import cloud.hytora.driver.networking.protocol.codec.PacketDecoder;
import cloud.hytora.driver.networking.protocol.codec.PacketEncoder;
import cloud.hytora.driver.networking.protocol.codec.prepender.NettyPacketLengthDeserializer;
import cloud.hytora.driver.networking.protocol.codec.prepender.NettyPacketLengthSerializer;
import cloud.hytora.driver.networking.protocol.packets.ConnectionState;
import cloud.hytora.driver.networking.protocol.packets.ConnectionType;

import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import cloud.hytora.driver.networking.protocol.packets.IPacket;
import cloud.hytora.driver.networking.protocol.packets.defaults.HandshakePacket;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.networking.protocol.wrapped.SimplePacketChannel;
import cloud.hytora.driver.networking.AbstractNetworkComponent;


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

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Getter
@Setter
public abstract class ClusterExecutor extends AbstractNetworkComponent<ClusterExecutor> implements EndpointNetworkExecutor {

    /**
     * The name of this node
     */
    private final String nodeName;

    /**
     * Authentication key
     */
    private final String authKey;

    /**
     * All cached clients
     */
    private final List<ClusterClientExecutor> allCachedConnectedClients;
    private final Map<ChannelHandlerContext, PacketChannel> cachedContexts = new HashMap<>();

    //netty stuff
    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;
    private EventExecutorGroup eventExecutorGroup;
    private SimplePacketChannel packetChannel;

    public ClusterExecutor(String authKey, String nodeName) {
        super(ConnectionType.NODE, nodeName);

        this.authKey = authKey;
        this.nodeName = nodeName;
        this.allCachedConnectedClients = new CopyOnWriteArrayList<>();

        this.packetChannel = new SimplePacketChannel();
        this.packetChannel.setState(ConnectionState.DISCONNECTED);
        this.packetChannel.setModificationTime(System.currentTimeMillis());
        this.packetChannel.setParticipant(this);
        this.packetChannel.setEverConnected(false);
        this.packetChannel.setWrapped(null);
    }

    public Task<ClusterExecutor> openConnection(String hostname, int port) {
        Task<ClusterExecutor> connectPromise = Task.empty();

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
                                                 ClusterClientExecutor clusterClientExecutor = addConnectedClient(ctx.channel());

                                                 SimplePacketChannel wrapper = new SimplePacketChannel();

                                                 wrapper.setParticipant(clusterClientExecutor);
                                                 wrapper.setWrapped(ctx);
                                                 wrapper.setModificationTime(System.currentTimeMillis());
                                                 wrapper.setState(ConnectionState.CONNECTED);
                                                 wrapper.setEverConnected(true);

                                                 cachedContexts.put(ctx, wrapper);
                                             }

                                             @Override
                                             public void channelRead0(ChannelHandlerContext channelHandlerContext, AbstractPacket packet) {

                                                 SimpleClusterClientExecutor client = (SimpleClusterClientExecutor) getConnectedClientByChannel(channelHandlerContext.channel());

                                                 if (client == null) {
                                                     channelHandlerContext.close();
                                                     return;
                                                 }

                                                 PacketChannel channel = cachedContexts.get(channelHandlerContext);
                                                 if (!client.isAuthenticated()) {

                                                     if (packet instanceof HandshakePacket) {
                                                         HandshakePacket authPacket = (HandshakePacket) packet;
                                                         if (!authPacket.getAuthKey().equalsIgnoreCase(authKey)) {
                                                             System.out.println(" ");
                                                             System.out.println("<===  WARNING   =====>");
                                                             System.out.println(StringUtils.format("Tried to authenticate '{0}' but a wrong AuthKey was provided", client.getName()));
                                                             System.out.println("Closing channel...");
                                                             System.out.println("<===  WARNING   =====>");
                                                             System.out.println(" ");
                                                             channelHandlerContext.close();
                                                             return;
                                                         }
                                                         client.setName(authPacket.getClientName());
                                                         client.setAuthenticated(true);
                                                         client.setType(authPacket.getType());
                                                         client.setData(authPacket.getExtraData());

                                                         //setting node name and sending back
                                                         authPacket.setNodeName(getNodeName());
                                                         client.sendPacket(authPacket);

                                                         handleConnectionChange(ConnectionState.CONNECTED, client, cachedContexts.get(channelHandlerContext));
                                                     } else {
                                                         System.out.println(" ");
                                                         System.out.println("<===  WARNING   =====>");
                                                         System.out.println(StringUtils.format("Tried to authenticate '{0}' but instead of {1}, the first Packet was a {2}", client.getName(), HandshakePacket.class.getName(), packet.getClass().getName()));
                                                         System.out.println("Closing channel...");
                                                         System.out.println("<===  WARNING   =====>");
                                                         System.out.println(" ");
                                                         channelHandlerContext.close();
                                                     }
                                                     return;
                                                 }
                                                 handlePacket(channel, packet);
                                             }

                                             @Override
                                             public void channelInactive(final ChannelHandlerContext ctx) {
                                                 closeClient(ctx);
                                                 cachedContexts.remove(ctx, packetChannel);
                                             }

                                             @Override
                                             public void channelUnregistered(final ChannelHandlerContext ctx) {
                                                 closeClient(ctx);
                                                 cachedContexts.remove(ctx, packetChannel);
                                             }
                                         }
                                );
                    }
                }).option(ChannelOption.SO_BACKLOG, 128)
                .bind(hostname, port).addListener(future -> {
                    this.packetChannel.setEverConnected(true);
                    this.packetChannel.setState(ConnectionState.CONNECTED);
                    CloudDriver.getInstance().getProviderRegistry().getUnchecked(IEventManager.class).callEventGlobally(new DriverConnectEvent());
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

        CloudDriver.getInstance().getProviderRegistry().getUnchecked(IEventManager.class).callEventGlobally(new DriverDisconnectEvent());
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
    public Task<Void> sendPacketAsync(IPacket packet) {
        return Task.callAsync(() -> {
            sendPacket(packet);
            return null;
        });
    }

    @Override
    public String getName() {
        return nodeName;
    }

    @Override
    public ConnectionType getType() {
        return ConnectionType.NODE;
    }

    public ClusterClientExecutor getConnectedClientByChannel(Channel channel) {
        return this.allCachedConnectedClients.stream().filter(it -> it.getChannel() == channel).findAny().orElse(null);
    }

    public void closeClient(ChannelHandlerContext context) {
        ClusterClientExecutor connectedClient = getConnectedClientByChannel(context.channel());
        if (connectedClient == null) {
            return;
        }
        this.handleConnectionChange(ConnectionState.DISCONNECTED, connectedClient, cachedContexts.get(context));
        this.allCachedConnectedClients.remove(connectedClient);
    }


    public List<ClusterClientExecutor> getAllClientsByType(ConnectionType type) {
        return this.getAllCachedConnectedClients()
                .stream()
                .filter(it -> it instanceof SimpleClusterClientExecutor)
                .map(it -> ((SimpleClusterClientExecutor) it))
                .filter(it -> it.getType().equals(type))
                .collect(Collectors.toList());
    }

    public Optional<ClusterClientExecutor> getClient(String name) {
        return this.getAllCachedConnectedClients().stream()
                .filter(client -> client.getName().equals(name)).findAny();
    }


    public void sendPacketToType(AbstractPacket packet, ConnectionType type) {
        this.getAllClientsByType(type).forEach(it -> it.sendPacket(packet));
    }

    public ClusterClientExecutor addConnectedClient(Channel channel) {
        ClusterClientExecutor clientExecutor = new SimpleClusterClientExecutor(channel);
        this.allCachedConnectedClients.add(clientExecutor);

        return clientExecutor;
    }




    public void sendPacketToAll(IPacket packet) {
        allCachedConnectedClients.forEach(it -> it.sendPacket(packet));
    }

    public abstract void handleConnectionChange(ConnectionState state, ClusterClientExecutor executor, PacketChannel wrapper);


}
