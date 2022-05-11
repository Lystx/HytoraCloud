package cloud.hytora.driver.networking.protocol.codec;

import cloud.hytora.driver.networking.protocol.packets.ConnectionState;
import cloud.hytora.driver.networking.protocol.packets.IPacket;
import cloud.hytora.driver.networking.protocol.packets.Packet;
import cloud.hytora.driver.networking.protocol.wrapped.SimpleChannelWrapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Getter;
import cloud.hytora.driver.networking.AbstractNetworkComponent;

import java.io.IOException;

@Getter
public class NetworkBossHandler extends SimpleChannelInboundHandler<IPacket> {

    /**
     * The parent participant for this handler
     */
    private final AbstractNetworkComponent<?> component;

    /**
     * The context of this handler
     */
    protected final SimpleChannelWrapper wrapper;

    public NetworkBossHandler(AbstractNetworkComponent<?> component) {
        this.component = component;

        this.wrapper = new SimpleChannelWrapper();

        this.wrapper.setModificationTime(System.currentTimeMillis());
        this.wrapper.setParticipant(component);
        this.wrapper.setState(ConnectionState.DISCONNECTED);
        this.wrapper.setEverConnected(false);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        this.wrapper.setState(ConnectionState.CONNECTED);
        this.wrapper.setWrapped(ctx);
        this.wrapper.setEverConnected(true);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        //to avoid disconnecting exceptions
        if (cause instanceof IOException) {
            return;
        }
        cause.printStackTrace();
    }

    @Override
    public void channelRead0(ChannelHandlerContext channelHandlerContext, IPacket packet)  {
        component.handlePacket(this.wrapper, packet);
    }
}
