package cloud.hytora.driver.config.def;

import cloud.hytora.driver.config.ISpigotConfig;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.types.BufferState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UniversalSpigotConfig implements ISpigotConfig {

    private String joinMessage;
    private boolean weather;
    private boolean peaceful;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        switch (state) {
            case READ:
                this.joinMessage = buf.readString();
                this.weather = buf.readBoolean();
                this.peaceful = buf.readBoolean();
                break;
            case WRITE:
                buf.writeString(joinMessage);
                buf.writeBoolean(weather);
                buf.writeBoolean(peaceful);
                break;
        }
    }
}
