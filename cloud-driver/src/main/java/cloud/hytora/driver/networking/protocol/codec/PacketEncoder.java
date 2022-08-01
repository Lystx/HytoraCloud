package cloud.hytora.driver.networking.protocol.codec;

import cloud.hytora.driver.networking.protocol.codec.buf.DefaultPacketBuffer;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import cloud.hytora.driver.networking.INetworkExecutor;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;

import java.io.IOException;


@AllArgsConstructor
public class PacketEncoder extends MessageToByteEncoder<AbstractPacket> {

    private final INetworkExecutor participant;

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, AbstractPacket packet, ByteBuf output) throws Exception {
        PacketBuffer buf = new DefaultPacketBuffer(output, this.participant);
        try {
            buf.writePacket(packet);
        } catch (IOException e) {
            System.out.println("Error");
            e.printStackTrace();
        }
    }
}
