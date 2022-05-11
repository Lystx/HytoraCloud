package cloud.hytora.driver.networking.cluster;

import cloud.hytora.common.misc.StringUtils;
import cloud.hytora.common.wrapper.Wrapper;
import cloud.hytora.driver.networking.EndpointNetworkExecutor;
import cloud.hytora.driver.networking.cluster.client.SimpleClusterClientExecutor;
import cloud.hytora.driver.networking.protocol.codec.NetworkBossHandler;
import cloud.hytora.driver.networking.protocol.codec.PacketDecoder;
import cloud.hytora.driver.networking.protocol.codec.PacketEncoder;
import cloud.hytora.driver.networking.protocol.codec.prepender.NettyPacketLengthDeserializer;
import cloud.hytora.driver.networking.protocol.codec.prepender.NettyPacketLengthSerializer;
import cloud.hytora.driver.networking.protocol.packets.ConnectionState;
import cloud.hytora.driver.networking.protocol.packets.ConnectionType;
import cloud.hytora.driver.networking.protocol.packets.IPacket;
import cloud.hytora.driver.networking.protocol.packets.Packet;
import cloud.hytora.driver.networking.protocol.packets.defaults.HandshakePacket;
import cloud.hytora.driver.networking.protocol.wrapped.ChannelWrapper;
import cloud.hytora.driver.networking.protocol.wrapped.SimpleChannelWrapper;
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
     * All cached clients
     */
    private final List<ClusterClientExecutor> allCachedConnectedClients;
    private final Map<ChannelHandlerContext, ChannelWrapper> cachedContexts = new HashMap<>();

    //netty stuff
    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;
    private EventExecutorGroup eventExecutorGroup;
    private SimpleChannelWrapper wrapper;

    public ClusterExecutor(String nodeName) {
        super(ConnectionType.NODE, nodeName);
        this.nodeName = nodeName;
        this.allCachedConnectedClients = new ArrayList<>();

        this.wrapper = new SimpleChannelWrapper();
        this.wrapper.setState(ConnectionState.DISCONNECTED);
        this.wrapper.setModificationTime(System.currentTimeMillis());
        this.wrapper.setParticipant(this);
        this.wrapper.setEverConnected(false);
        this.wrapper.setWrapped(null);
    }

    public Wrapper<ClusterExecutor> openConnection(String hostname, int port) {
        Wrapper<ClusterExecutor> connectPromise = Wrapper.empty();
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
                                                 ClusterClientExecutor clusterClientExecutor = addConnectedClient(ctx.channel());

                                                 SimpleChannelWrapper wrapper = new SimpleChannelWrapper();

                                                 wrapper.setParticipant(clusterClientExecutor);
                                                 wrapper.setWrapped(ctx);
                                                 wrapper.setModificationTime(System.currentTimeMillis());
                                                 wrapper.setState(ConnectionState.CONNECTED);
                                                 wrapper.setEverConnected(true);

                                                 cachedContexts.put(ctx, wrapper);
                                             }

                                             @Override
                                             public void channelRead0(ChannelHandlerContext channelHandlerContext, IPacket packet) {
                                                 SimpleClusterClientExecutor client = (SimpleClusterClientExecutor) getConnectedClientByChannel(channelHandlerContext.channel());

                                                 if (client == null) {
                                                     channelHandlerContext.close();
                                                     return;
                                                 }

                                                 if (!client.isAuthenticated()) {

                                                     if (packet instanceof HandshakePacket) {
                                                         HandshakePacket authPacket = (HandshakePacket) packet;
                                                         client.setName(authPacket.getClientName());
                                                         client.setAuthenticated(true);
                                                         client.setType(authPacket.getType());
                                                         client.setData(authPacket.getExtraData());

                                                         client.sendPacket(packet);
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
                                                 handlePacket(cachedContexts.get(channelHandlerContext), packet);
                                             }

                                             @Override
                                             public void channelInactive(final ChannelHandlerContext ctx) {
                                                 closeClient(ctx);
                                                 cachedContexts.remove(ctx, wrapper);
                                             }

                                             @Override
                                             public void channelUnregistered(final ChannelHandlerContext ctx) {
                                                 closeClient(ctx);
                                                 cachedContexts.remove(ctx, wrapper);
                                             }
                                         }
                                );
                    }
                }).option(ChannelOption.SO_BACKLOG, 128)
                .bind(hostname, port).addListener(future -> {
                    this.wrapper.setEverConnected(true);
                    this.wrapper.setState(ConnectionState.CONNECTED);
                    if (future.isSuccess()) {
                        connectPromise.setResult(this);
                    } else {
                        connectPromise.setFailure(future.cause());
                    }
                });

        return connectPromise;
    }

    public Wrapper<Boolean> shutdown() {
        Wrapper<Boolean> shutdownBossPromise = Wrapper.empty();
        Wrapper<Boolean> shutdownWorkerPromise = Wrapper.empty();
        Wrapper<Boolean> executePromise = Wrapper.empty();

        Wrapper<Boolean> promise = Wrapper.multiTasking(shutdownBossPromise, shutdownBossPromise, executePromise);

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


    public void sendPacketToType(Packet packet, ConnectionType type) {
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

    public abstract void handleConnectionChange(ConnectionState state, ClusterClientExecutor executor, ChannelWrapper wrapper);


}
