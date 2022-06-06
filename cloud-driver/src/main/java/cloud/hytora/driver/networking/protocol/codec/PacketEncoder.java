package cloud.hytora.driver.networking.protocol.codec;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.DriverEnvironment;
import cloud.hytora.driver.networking.protocol.codec.buf.DefaultPacketBuffer;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.Packet;
import cloud.hytora.driver.networking.NetworkExecutor;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;

import java.io.IOException;


@AllArgsConstructor
public class PacketEncoder extends MessageToByteEncoder<Packet> {

    private final NetworkExecutor participant;

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Packet packet, ByteBuf output) throws Exception{
        if (CloudDriver.getInstance().getEnvironment() == DriverEnvironment.SERVICE) {
            System.out.println("Encoding " + packet.getClass().getSimpleName());
        }
        PacketBuffer buf = new DefaultPacketBuffer(output, this.participant);
        try {
            buf.writePacket(packet);
        } catch (IOException e) {
            System.out.println("Error");
            e.printStackTrace();
        }
    }
}
