package cloud.hytora.driver.config.def;

import cloud.hytora.driver.config.IJavaVersion;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.types.BufferState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@AllArgsConstructor
@Getter
@NoArgsConstructor
public class UniversalJavaVersion implements IJavaVersion {

    private String name;
    private String path;
    private int id;


    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {

        switch (state) {

            case READ:
                name = buf.readString();
                path = buf.readString();
                id = buf.readInt();
                break;

            case WRITE:
                buf.writeString(name);
                buf.writeString(path);
                buf.writeInt(id);
                break;
        }
    }

}
