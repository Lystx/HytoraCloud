package cloud.hytora.driver.networking.protocol.wrapped;

import cloud.hytora.common.task.ITask;
import cloud.hytora.document.Document;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.IPacket;
import cloud.hytora.driver.networking.protocol.packets.NetworkResponseState;
import cloud.hytora.driver.networking.protocol.packets.ConnectionType;

import java.util.function.Consumer;

public interface ChanneledPacketAction<R> {

    ChanneledPacketAction<R> state(NetworkResponseState state);

    ChanneledPacketAction<R> error(Throwable state);

    ChanneledPacketAction<R> data(Document document);

    ChanneledPacketAction<R> buffer(Consumer<PacketBuffer> buf);

    ChanneledPacketAction<R> buffer(PacketBuffer buf);

    ChanneledPacketAction<R> receivers(String... receivers);

    ChanneledPacketAction<R> receivers(ConnectionType... types);

    ITask<R> execute(IPacket packet);

}
