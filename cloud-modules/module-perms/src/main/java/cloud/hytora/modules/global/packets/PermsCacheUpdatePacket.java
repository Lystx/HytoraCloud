package cloud.hytora.modules.global.packets;

import cloud.hytora.document.Document;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import cloud.hytora.driver.networking.protocol.packets.BufferPacket;
import cloud.hytora.driver.networking.protocol.packets.PacketProperty;
import cloud.hytora.driver.networking.protocol.types.BufferState;
import cloud.hytora.driver.module.permission.PermissionGroup;
import cloud.hytora.modules.global.impl.DefaultPermissionGroup;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

@NoArgsConstructor
@Getter
public class PermsCacheUpdatePacket extends BufferPacket {
    public PermsCacheUpdatePacket(Collection<PermissionGroup> permissionGroups) {
        super(buf -> buf.writeObjectCollection(permissionGroups));
    }


}
