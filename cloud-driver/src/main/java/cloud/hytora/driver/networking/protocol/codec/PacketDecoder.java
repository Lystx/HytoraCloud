package cloud.hytora.driver.networking.protocol.codec;

import cloud.hytora.driver.networking.protocol.codec.buf.DefaultPacketBuffer;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.Packet;
import cloud.hytora.driver.networking.NetworkExecutor;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class PacketDecoder extends ByteToMessageDecoder {

    private final NetworkExecutor participant;

    @Override
    protected void decode(ChannelHandlerContext context, ByteBuf byteBuf, List<Object> list) throws Exception {
        if (context != null && (!context.channel().isActive() || !byteBuf.isReadable())) {
            byteBuf.clear();
            return;
        }

        try {
            PacketBuffer buf = new DefaultPacketBuffer(byteBuf, this.participant);
            Packet packet = buf.readPacket();
            if (packet == null) {
                return;
            }
            list.add(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

