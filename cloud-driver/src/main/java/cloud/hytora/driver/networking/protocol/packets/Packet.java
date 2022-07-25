package cloud.hytora.driver.networking.protocol.packets;

import cloud.hytora.common.task.Task;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.protocol.codec.buf.Bufferable;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

@Setter
@Getter
@Accessors(fluent = true)
public abstract class Packet implements Bufferable {

    /**
     * The transfer info of this packet
     */
    protected SimplePacketTransferInfo transferInfo;

    /**
     * The copied buffer before reading packet
     */
    protected PacketBuffer buffer;

    /**
     * The channel this is going to be sent to
     */
    protected String destinationChannel;

    public Packet() {
        this.buffer = PacketBuffer.unsafe();
        this.transferInfo = new SimplePacketTransferInfo(
                UUID.randomUUID(),
                CloudDriver.getInstance() == null ? null : CloudDriver.getInstance().getExecutor(),
                DocumentFactory.newJsonDocument()
        );
        this.destinationChannel = "global_packet_channel";
    }

    public Packet(Consumer<PacketBuffer> buffer) {
        this();
        buffer.accept(this.buffer);
    }



    public Task<Void> publish() {
        return Task.callSync(() -> {
            CloudDriver.getInstance().getExecutor().sendPacket(Packet.this);
            return null;
        });
    }

    public Task<Void> publishAsync() {
        return Task.callAsync(() -> {
            CloudDriver.getInstance().getExecutor().sendPacket(Packet.this);
            return null;
        });
    }
}
