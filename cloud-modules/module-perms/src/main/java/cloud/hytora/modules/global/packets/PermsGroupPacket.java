package cloud.hytora.modules.global.packets;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.networking.protocol.packets.Packet;
import cloud.hytora.driver.permission.PermissionGroup;
import cloud.hytora.modules.global.impl.DefaultPermissionGroup;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.IOException;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PermsGroupPacket extends Packet {

    private PayLoad payLoad;
    private PermissionGroup group;

    private String name;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        switch (state) {
            case READ:
                this.payLoad = buf.readEnum(PayLoad.class);
                this.group = buf.readOptionalObject(DefaultPermissionGroup.class);
                this.name = buf.readString();
                break;
            case WRITE:
                buf.writeEnum(payLoad);
                buf.writeOptionalObject(group);
                buf.writeString(name);
                break;
        }
    }


    public enum PayLoad {

        REMOVE,

        UPDATE,

        CREATE

    }
}
