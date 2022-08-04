package cloud.hytora.driver.message;

import cloud.hytora.document.Document;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.UUID;
import java.util.function.Consumer;

@Setter
@Accessors(fluent = true)
public class ChannelMessageBuilder {

    /**
     * The channel
     */
    private String channel;

    /**
     * The key
     */
    private String key;

    /**
     * The data
     */
    private Document document;

    /**
     * The receiver
     */
    private NetworkComponent[] receivers = new NetworkComponent[0];

    private PacketBuffer buffer = PacketBuffer.unsafe();


    public ChannelMessageBuilder buffer(Consumer<PacketBuffer> buffer) {
        this.buffer.append(buffer);
        return this;
    }

    /**
     * Builds the {@link ChannelMessage}
     *
     * @return built message
     */
    public ChannelMessage build() {
        return new DefaultChannelMessage(key, this.channel, this.document, this.buffer, receivers, CloudDriver.getInstance().getExecutor(), UUID.randomUUID());
    }
}
