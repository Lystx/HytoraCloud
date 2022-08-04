package cloud.hytora.modules.sign.api;

import cloud.hytora.driver.message.ChannelMessage;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.modules.sign.api.config.SignConfiguration;
import cloud.hytora.modules.sign.api.protocol.SignProtocolType;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Consumer;

@Getter
@Setter
public abstract class CloudSignAPI {

    /**
     * The channel name to exchange data between module <==> server
     */
    public static final String CHANNEL_NAME = "cloud::modules::signs::channel";

    @Getter
    /**
     * The static instance field
     */
    private static CloudSignAPI instance;

    /**
     * The config where data of this module is stored in
     */
    private SignConfiguration signConfiguration;

    /**
     * Constructs a new instance
     * and sets the config to a default value in case
     * something goes wrong and the config does not get send or something
     */
    public CloudSignAPI() {
        instance = this;

        this.signConfiguration = new SignConfiguration(); //create default as fallback
    }

    /**
     * publishes the {@link SignConfiguration} to the other side
     */
    public abstract void publishConfiguration();

    /**
     * The current {@link ICloudSignManager} instance to
     * manage all your {@link ICloudSign}s
     */
    public abstract ICloudSignManager getSignManager();


    public abstract void performProtocolAction(SignProtocolType type, Consumer<PacketBuffer> buffer);
}
