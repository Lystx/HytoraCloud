package cloud.hytora.driver.networking.protocol.wrapped;

import cloud.hytora.common.wrapper.Wrapper;
import cloud.hytora.document.Document;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.QueryState;
import cloud.hytora.driver.networking.protocol.packets.ConnectionType;
import cloud.hytora.driver.networking.protocol.packets.Packet;

import java.util.function.Consumer;

public interface ChanneledPacketAction<R> {

    ChanneledPacketAction<R> state(QueryState state);

    ChanneledPacketAction<R> data(Document document);

    ChanneledPacketAction<R> buffer(Consumer<PacketBuffer> buf);

    ChanneledPacketAction<R> buffer(PacketBuffer buf);

    ChanneledPacketAction<R> receivers(String... receivers);

    ChanneledPacketAction<R> receivers(ConnectionType... types);

    Wrapper<R> execute(Packet packet);

}
