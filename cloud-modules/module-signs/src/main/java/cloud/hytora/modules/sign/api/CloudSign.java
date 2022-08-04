package cloud.hytora.modules.sign.api;

import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class CloudSign implements IBufferObject {

    /**
     * The uuid of this sign
     */
    private UUID uuid;

    /**
     * The task for it
     */
    private String task;

    /**
     * the xyz location
     */
    private int x, y, z;

    /**
     * The world of the location
     */
    private String world;

    public CloudSign(Integer x, Integer y, Integer z, String task, String world) {
        this(UUID.randomUUID(), task, x, y, z, world);
    }

    public CloudSign() {
        this(0, 0, 0, "", "");
    }


    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        switch (state) {
            case READ:
                this.uuid = buf.readUniqueId();
                this.task = buf.readString();
                this.x = buf.readInt();
                this.y = buf.readInt();
                this.z = buf.readInt();
                this.world = buf.readString();
                break;
            case WRITE:
                buf.writeUniqueId(uuid);
                buf.writeString(task);
                buf.writeInt(x);
                buf.writeInt(y);
                buf.writeInt(z);
                buf.writeString(world);
                break;
        }
    }
}
