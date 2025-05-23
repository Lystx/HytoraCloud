package cloud.hytora.driver.networking.protocol.packets;

import cloud.hytora.common.task.Task;
import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.EndpointNetworkExecutor;
import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.networking.packets.RedirectPacket;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;

import java.util.UUID;

import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.function.Consumer;

@Setter
@Getter
@Accessors(fluent = true)
public abstract class AbstractPacket implements IPacket {

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

    /**
     * the channel that received the packet
     */
    @Setter
    public PacketChannel channel;

    public AbstractPacket() {
        this.buffer = PacketBuffer.unsafe();
        this.transferInfo = new SimplePacketTransferInfo(
                UUID.randomUUID(),
                CloudDriver.getInstance() == null ? null : CloudDriver.getInstance().getExecutor(),
                DocumentFactory.newJsonDocument()
        );
        this.destinationChannel = "global_packet_channel";
    }

    public AbstractPacket(Consumer<PacketBuffer> buffer) {
        this();
        buffer.accept(this.buffer);
    }

    public String getDestinationChannel() {
        return destinationChannel;
    }

    public void setDestinationChannel(String destinationChannel) {
        this.destinationChannel = destinationChannel;
    }

    public void publish() {
        Task.callSync(() -> {
            CloudDriver.getInstance().getExecutor().sendPacket(AbstractPacket.this);
            return null;
        });
    }

    @Override
    public void publishTo(String... receivers) {
        if (CloudDriver.getInstance().getEnvironment() == CloudDriver.Environment.NODE) {
            EndpointNetworkExecutor executor = (EndpointNetworkExecutor) CloudDriver.getInstance().getExecutor();
            for (String receiver : receivers) {

                executor.sendPacket(this, NetworkComponent.of(receiver));
            }
        }
        for (String receiver : receivers) {
            CloudDriver.getInstance().getExecutor().sendPacket(new RedirectPacket(receiver, this));
        }
    }

    public Task<Void> publishAsync() {
        return Task.callAsync(() -> {
            CloudDriver.getInstance().getExecutor().sendPacket(AbstractPacket.this);
            return null;
        });
    }

    public Task<Void> respond(NetworkResponseState state, Consumer<PacketBuffer> bufferConsumer) {
        PacketBuffer packetBuffer = PacketBuffer.unPooled();
        bufferConsumer.accept(packetBuffer);
        return channel.prepareResponse().buffer(packetBuffer).state(state).execute(this);
    }
    public Task<Void> respond(NetworkResponseState state) {
        return channel.prepareResponse().state(state).execute(this);
    }

    public Task<Void> respond(NetworkResponseState state, Document data) {
        return channel.prepareResponse().data(data).state(state).execute(this);
    }

    public Task<Void> respond(NetworkResponseState state, Document data, Consumer<PacketBuffer> bufferConsumer) {
        PacketBuffer packetBuffer = PacketBuffer.unPooled();
        bufferConsumer.accept(packetBuffer);
        return channel.prepareResponse().buffer(packetBuffer).data(data).state(state).execute(this);
    }


    public Task<BufferedResponse> awaitResponse() {
        return CloudDriver.getInstance().getExecutor().getPacketChannel().prepareSingleQuery().execute(this);
    }

    @Override
    public Task<BufferedResponse> awaitResponse(String receiver) {
        return CloudDriver.getInstance().getExecutor().getPacketChannel().prepareSingleQuery().receivers(receiver).execute(this);
    }
}
