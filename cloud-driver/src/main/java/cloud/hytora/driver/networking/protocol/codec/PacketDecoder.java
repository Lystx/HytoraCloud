package cloud.hytora.driver.networking.protocol.codec;

import cloud.hytora.driver.networking.protocol.codec.buf.DefaultPacketBuffer;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import cloud.hytora.driver.networking.INetworkExecutor;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class PacketDecoder extends ByteToMessageDecoder {

    private final INetworkExecutor participant;

    @Override
    protected void decode(ChannelHandlerContext context, ByteBuf byteBuf, List<Object> list) throws Exception {
        if (context != null && (!context.channel().isActive() || !byteBuf.isReadable())) {
            byteBuf.clear();
            return;
        }

        try {
            PacketBuffer buf = new DefaultPacketBuffer(byteBuf, this.participant);
            AbstractPacket packet = buf.readPacket();
            if (packet == null) {
                System.out.println("Couldn't decode Packet");
                return;
            }
            list.add(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

