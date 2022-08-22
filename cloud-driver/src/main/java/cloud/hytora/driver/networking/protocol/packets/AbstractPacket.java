package cloud.hytora.driver.networking.protocol.packets;

import cloud.hytora.common.task.Task;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.DriverEnvironment;
import cloud.hytora.driver.networking.EndpointNetworkExecutor;
import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.networking.packets.RedirectPacket;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;

import java.util.UUID;

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

    public AbstractPacket() {
        this.buffer = PacketBuffer.unsafe();
        this.transferInfo = new SimplePacketTransferInfo(
                UUID.randomUUID(),
                CloudDriver.getInstance() == null ? null : CloudDriver.getInstance().getNetworkExecutor(),
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
            CloudDriver.getInstance().getNetworkExecutor().sendPacket(AbstractPacket.this);
            return null;
        });
    }

    @Override
    public void publishTo(String... receivers) {
        if (CloudDriver.getInstance().getEnvironment() == DriverEnvironment.NODE) {
            EndpointNetworkExecutor executor = (EndpointNetworkExecutor) CloudDriver.getInstance().getNetworkExecutor();
            for (String receiver : receivers) {

                executor.sendPacket(this, NetworkComponent.of(receiver));
            }
        }
        for (String receiver : receivers) {
            CloudDriver.getInstance().getNetworkExecutor().sendPacket(new RedirectPacket(receiver, this));
        }
    }

    public Task<Void> publishAsync() {
        return Task.callAsync(() -> {
            CloudDriver.getInstance().getNetworkExecutor().sendPacket(AbstractPacket.this);
            return null;
        });
    }

    public Task<BufferedResponse> awaitResponse() {
        return CloudDriver.getInstance().getNetworkExecutor().getPacketChannel().prepareSingleQuery().execute(this);
    }

    @Override
    public Task<BufferedResponse> awaitResponse(String receiver) {
        return CloudDriver.getInstance().getNetworkExecutor().getPacketChannel().prepareSingleQuery().receivers(receiver).execute(this);
    }
}
