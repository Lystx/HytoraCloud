package cloud.hytora.driver.networking.protocol.codec.prepender;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class NettyPacketLengthDeserializer extends ByteToMessageDecoder {

    private final int byteSize = 5;

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) {
        try {

            if (!channelHandlerContext.channel().isActive()) {
                byteBuf.skipBytes(byteBuf.readableBytes());
                return;
            }
            if (!byteBuf.isReadable()) {
                return;
            }

            int readerIndex = byteBuf.readerIndex();
            byte[] bytes = new byte[byteSize];

            for (int i = 0; i < byteSize; i++) {
                if (!byteBuf.isReadable()) {
                    byteBuf.readerIndex(readerIndex);
                    return;
                }

                bytes[i] = byteBuf.readByte();
                if (bytes[i] >= 0) {
                    ByteBuf buf = Unpooled.wrappedBuffer(bytes);

                    try {
                        int length = this.readVarIntUnchecked(buf);

                        if (byteBuf.readableBytes() < length) {
                            byteBuf.readerIndex(readerIndex);
                            return;
                        }

                        list.add(byteBuf.readBytes(length));
                    } finally {
                        buf.release();
                    }

                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Integer readVarIntUnchecked(ByteBuf byteBuf) {
        int i = 0;
        int maxRead = Math.min(byteSize, byteBuf.readableBytes());
        for (int j = 0; j < maxRead; j++) {
            int k = byteBuf.readByte();
            i |= (k & 127) << j * 7;
            if ((k & 128) != 128) {
                return i;
            }
        }

        return null;
    }

}
