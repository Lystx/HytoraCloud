package cloud.hytora.driver.networking.cluster.client;

import cloud.hytora.common.collection.ThreadRunnable;
import cloud.hytora.common.misc.Util;
import cloud.hytora.common.task.Task;
import cloud.hytora.document.Document;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.event.defaults.driver.DriverConnectEvent;
import cloud.hytora.driver.networking.AbstractNetworkComponent;
import cloud.hytora.driver.networking.protocol.codec.NetworkBossHandler;
import cloud.hytora.driver.networking.protocol.codec.PacketDecoder;
import cloud.hytora.driver.networking.protocol.codec.PacketEncoder;
import cloud.hytora.driver.networking.protocol.codec.prepender.NettyPacketLengthDeserializer;
import cloud.hytora.driver.networking.protocol.codec.prepender.NettyPacketLengthSerializer;
import cloud.hytora.driver.networking.protocol.packets.ConnectionType;
import cloud.hytora.driver.networking.protocol.packets.IPacket;
import cloud.hytora.driver.networking.protocol.packets.defaults.HandshakePacket;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.nio.channels.AlreadyConnectedException;

@Getter
public abstract class AdvancedClusterParticipant extends ClusterParticipant {

    public AdvancedClusterParticipant(String authKey, String clientName, ConnectionType type, Document customData) {
        super(authKey, clientName, type, customData);
    }

    public abstract void onAuthenticationChanged(PacketChannel wrapper);

    public abstract void onActivated(ChannelHandlerContext ctx);

    public abstract void onClose(ChannelHandlerContext ctx);

}
