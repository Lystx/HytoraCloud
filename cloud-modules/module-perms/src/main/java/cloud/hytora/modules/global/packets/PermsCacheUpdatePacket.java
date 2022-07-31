package cloud.hytora.modules.global.packets;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import cloud.hytora.driver.permission.PermissionGroup;
import cloud.hytora.driver.permission.PermissionPlayer;
import cloud.hytora.modules.global.impl.DefaultPermissionGroup;
import cloud.hytora.modules.global.impl.DefaultPermissionPlayer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PermsCacheUpdatePacket extends AbstractPacket {

    private Collection<PermissionGroup> permissionGroups;
    private Collection<PermissionPlayer> permissionPlayers;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        switch (state) {
            case WRITE:
                buf.writeObjectCollection(permissionGroups);
                buf.writeObjectCollection(permissionPlayers);
                break;

            case READ:
                permissionGroups = buf.readWrapperObjectCollection(DefaultPermissionGroup.class);
                permissionPlayers = buf.readWrapperObjectCollection(DefaultPermissionPlayer.class);
                break;
        }
    }
}
