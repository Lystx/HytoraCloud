package cloud.hytora.driver.networking.protocol.codec;

import cloud.hytora.driver.networking.protocol.packets.ConnectionState;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import cloud.hytora.driver.networking.protocol.wrapped.SimplePacketChannel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Getter;
import cloud.hytora.driver.networking.AbstractNetworkComponent;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;

@Getter
public class NetworkBossHandler extends SimpleChannelInboundHandler<AbstractPacket> {

    /**
     * The parent participant for this handler
     */
    private final AbstractNetworkComponent<?> component;

    /**
     * The context of this handler
     */
    protected final SimplePacketChannel packetChannel;

    public NetworkBossHandler(AbstractNetworkComponent<?> component) {
        this.component = component;

        this.packetChannel = new SimplePacketChannel();

        this.packetChannel.setModificationTime(System.currentTimeMillis());
        this.packetChannel.setParticipant(component);
        this.packetChannel.setState(ConnectionState.DISCONNECTED);
        this.packetChannel.setEverConnected(false);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        this.packetChannel.setState(ConnectionState.CONNECTED);
        this.packetChannel.setWrapped(ctx);
        this.packetChannel.setEverConnected(true);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        //to avoid disconnecting exceptions
        if (cause instanceof IOException || cause instanceof ClosedChannelException || cause.getClass().getSimpleName().equalsIgnoreCase("StacklessClosedChannelException")) {
            return;
        }
        cause.printStackTrace();
    }

    @Override
    public void channelRead0(ChannelHandlerContext channelHandlerContext, AbstractPacket packet)  {
        component.handlePacket(this.packetChannel, packet);
    }
}
