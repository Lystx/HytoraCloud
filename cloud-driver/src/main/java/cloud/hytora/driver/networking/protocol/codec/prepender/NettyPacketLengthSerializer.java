package cloud.hytora.driver.networking.protocol.codec.prepender;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class NettyPacketLengthSerializer extends MessageToByteEncoder<ByteBuf> {

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) {
        try {
            final int readable = in.readableBytes();

            out.ensureWritable(readable + this.getVarIntSize(readable));
            writeVarInt(out, readable);
            out.writeBytes(in, in.readerIndex(), readable);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeVarInt(final ByteBuf buf, int value) {
        while (true) {
            if ((value & -128) == 0) {
                buf.writeByte(value);
                return;
            }

            buf.writeByte(value & 127 | 128);
            value >>>= 7;
        }
    }

    private int getVarIntSize(final int value) {
        if ((value & 0xffffff80) == 0) {
            return 1;
        } else if ((value & 0xffffc000) == 0) {
            return 2;
        } else if ((value & 0xffe00000) == 0) {
            return 3;
        } else if ((value & 0xf0000000) == 0) {
            return 4;
        } else {
            return 5;
        }
    }

}
