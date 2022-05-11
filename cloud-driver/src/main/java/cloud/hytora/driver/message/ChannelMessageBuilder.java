package cloud.hytora.driver.message;

import cloud.hytora.document.Document;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.NetworkComponent;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.UUID;

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
    private NetworkComponent[] receivers;

    /**
     * Builds the {@link ChannelMessage}
     *
     * @return built message
     */
    public ChannelMessage build() {
        return new DefaultChannelMessage(key, this.channel, this.document, receivers, CloudDriver.getInstance().getExecutor(), UUID.randomUUID());
    }
}
