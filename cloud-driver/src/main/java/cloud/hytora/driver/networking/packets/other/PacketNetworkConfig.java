package cloud.hytora.driver.networking.packets.other;

import cloud.hytora.driver.config.INetworkConfig;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import cloud.hytora.driver.networking.protocol.types.BufferState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.function.Consumer;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PacketNetworkConfig extends AbstractPacket {


    public static PacketNetworkConfig forUpdateConfig(INetworkConfig config, boolean sendBack) {
        return new PacketNetworkConfig(PayLoad.UPDATE, buf -> buf.writeObject(config).writeBoolean(sendBack));
    }

    public static PacketNetworkConfig forGetConfig() {
        return new PacketNetworkConfig(PayLoad.GET);
    }


    @Getter
    private PayLoad payLoad;

    public PacketNetworkConfig(PayLoad payLoad, Consumer<PacketBuffer> buffer) {
        super(buffer);
        this.payLoad = payLoad;
    }

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        switch (state) {
            case WRITE:
                buf.writeEnum(payLoad);
                break;
            case READ:
                payLoad = buf.readEnum(PayLoad.class);
                break;
        }
    }

    public enum PayLoad {

        UPDATE,

        GET

    }
}
