package cloud.hytora.driver.services.task;

import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class TaskDownloadEntry implements IBufferObject {

    private String url;
    private String destination;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        switch (state) {
            case WRITE:
                buf.writeString(url);
                buf.writeString(destination);
                break;
            case READ:
                url = buf.readString();
                destination = buf.readString();
                break;
        }
    }
}
