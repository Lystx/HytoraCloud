package cloud.hytora.driver.networking.protocol.packets;

import cloud.hytora.common.DriverUtility;
import cloud.hytora.common.task.Task;
import cloud.hytora.document.Document;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.*;
import cloud.hytora.driver.networking.packets.other.PacketRedirecting;
import cloud.hytora.driver.networking.packets.response.BufferedResponse;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import cloud.hytora.driver.networking.protocol.packets.info.PacketInfo;
import cloud.hytora.driver.networking.protocol.wrapped.PacketAction;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@Setter
@Getter
@Accessors(fluent = true)
/**
 * {@link AbstractPacket} is the implementation of an {@link IPacket}
 *
 * @since DEV-0.1
 * @version DEV-1.0
 */
public abstract class AbstractPacket implements IPacket {

    /**
     * The transfer info of this packet
     */
    protected PacketInfo transferInfo;

    /**
     * The copied buffer before reading packet
     */
    protected PacketBuffer buffer;

    /**
     * the channel that received the packet
     */
    @Setter
    public PacketChannel channel;

    /**
     * Constructs an empty packet
     */
    public AbstractPacket() {
        this(new PacketProperty[0]);
    }

    public AbstractPacket(Consumer<PacketBuffer> buffer) {
        this();
        buffer.accept(this.buffer);
    }

    public AbstractPacket(PacketProperty... properties) {
        List<PacketProperty> pp = Arrays.asList(properties);

        this.buffer = DriverUtility.get(pp.contains(PacketProperty.NO_BUFFER), () -> null, PacketBuffer::unPooled);
        this.transferInfo = new PacketInfo(
                UUID.randomUUID(),
                CloudDriver.getInstance() == null ? null : CloudDriver.getInstance().getExecutor(),
                Document.gson()
        );
    }

    @Override
    public void publish() {
        Task.callSync(() -> {
            CloudDriver.getInstance().getExecutor().sendPacket(AbstractPacket.this);
            return null;
        });
    }

    @Override
    public void publishToAll() {
        publishTo("ALL");
    }

    @Override
    public void publishTo(String... receivers) {
        HandlingNetworkExecutor networkExecutor = CloudDriver.getInstance().getExecutor();
        for (String receiver : receivers) {

            if (networkExecutor instanceof Cluster) {
                Cluster cluster = (Cluster) networkExecutor;
                if (receiver.equalsIgnoreCase("ALL")) {
                    cluster.sendPacketToAll(this);
                    continue;
                }
                cluster.sendPacket(this, receiver);
            } else {
                PacketRedirecting redirecting = new PacketRedirecting(receiver, this);
                redirecting.publish();
            }
        }
    }

    @Override
    public Task<Void> publishAsync() {
        return Task.callAsync(() -> {
            CloudDriver.getInstance().getExecutor().sendPacket(AbstractPacket.this);
            return null;
        });
    }

    @Override
    public PacketAction<Void> sendResponse() {
        return channel.sendResponse(this);
    }

    @Override
    public PacketAction<BufferedResponse> sendQuery() {
        return CloudDriver.getInstance().getExecutor().getPacketChannel().sendQuery(this);
    }

    @Override
    public @NotNull PacketBuffer getBufferSafe() {
        return buffer == null ? PacketBuffer.unPooled() : buffer;
    }

}
